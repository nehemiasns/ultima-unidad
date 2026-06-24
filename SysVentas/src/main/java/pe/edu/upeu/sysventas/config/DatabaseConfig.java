package pe.edu.upeu.sysventas.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Properties;

/**
 * Configuración de base de datos: HikariCP + DDL automático.
 *
 * Lee toda la configuración de application.properties (classpath).
 * Crea el pool HikariCP con HikariDataSource (compatible con GraalVM Native).
 * Si db.ddl.auto=true, ejecuta schema.sql al arrancar (idempotente con IF NOT EXISTS).
 *
 * Compatible con GraalVM Native Image:
 * - Sin Hibernate, sin ByteBuddy, sin reflexión dinámica de ORM.
 * - HikariCP 5.x incluye metadata para native-image en su JAR.
 * - H2 2.x tiene metadata en el GraalVM Reachability Repository.
 */
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);
    private static HikariDataSource dataSource;
    private static Server h2Server;

    private DatabaseConfig() {}

    /**
     * Inicializa el pool de conexiones leyendo application.properties.
     * Llama a este método una sola vez al arrancar la app.
     */
    public static synchronized void init() {
        if (dataSource != null && !dataSource.isClosed()) {
            return;
        }

        Properties props = loadProperties("application.properties");

        // Se comenta la inicialización del servidor TCP para evitar que procesos queden bloqueados en segundo plano
        /*
        boolean h2ServerEnabled = Boolean.parseBoolean(props.getProperty("db.h2server.enabled", "false"));
        if (h2ServerEnabled) {
            String port = props.getProperty("db.h2server.port", "9092");
            try {
                h2Server = Server.createTcpServer(
                        "-tcp",
                        "-tcpAllowOthers",
                        "-tcpPort", port
                ).start();
                log.info("H2 TCP Server iniciado en puerto {}", port);
            } catch (SQLException e) {
                throw new RuntimeException("No se pudo iniciar H2 TCP Server", e);
            }
        }
        */

        HikariConfig config = new HikariConfig();
        // Configuración de la URL en modo Embebido (File-based) con AUTO_SERVER=TRUE
        config.setJdbcUrl("jdbc:h2:file:./data/base_de_datos_tienda;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE;TRACE_LEVEL_FILE=0");
        config.setDriverClassName(props.getProperty("db.driver", "org.h2.Driver"));
        config.setUsername(props.getProperty("db.username", "sa"));
        config.setPassword(props.getProperty("db.password", ""));
        config.setMaximumPoolSize(
                Integer.parseInt(props.getProperty("db.pool.maximumPoolSize", "5")));
        config.setMinimumIdle(
                Integer.parseInt(props.getProperty("db.pool.minimumIdle", "1")));
        config.setConnectionTimeout(
                Long.parseLong(props.getProperty("db.pool.connectionTimeout", "30000")));
        config.setIdleTimeout(
                Long.parseLong(props.getProperty("db.pool.idleTimeout", "600000")));
        config.setMaxLifetime(
                Long.parseLong(props.getProperty("db.pool.maxLifetime", "1800000")));

        // Nombre del pool (aparece en logs)
        config.setPoolName("SysVentasPool");

        dataSource = new HikariDataSource(config);
        log.info("HikariCP pool '{}' iniciado — url: {}", config.getPoolName(),
                props.getProperty("db.url"));

        // DDL automático
        boolean ddlAuto = Boolean.parseBoolean(props.getProperty("db.ddl.auto", "true"));
        if (ddlAuto) {
            String script = props.getProperty("db.ddl.script", "schema.sql");
            runDdlScript(script);
        }
    }

    /**
     * Devuelve el DataSource de HikariCP.
     * Se usa directamente en repositorios y en JasperReports.
     */
    public static DataSource getDataSource() {
        if (dataSource == null || dataSource.isClosed()) {
            init();
        }
        return dataSource;
    }

    /** Obtiene una conexión del pool. El llamador debe cerrarla (try-with-resources). */
    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    /** Cierra el pool al apagar la app. */
    public static synchronized void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            log.info("Cerrando HikariCP pool...");
            dataSource.close();
        }
        if (h2Server != null && h2Server.isRunning(false)) {
            log.info("Deteniendo H2 TCP Server...");
            h2Server.stop();
        }
    }

    // ── Privados ─────────────────────────────────────────────────────────────

    private static Properties loadProperties(String filename) {
        Properties props = new Properties();
        try (InputStream is = DatabaseConfig.class
                .getClassLoader().getResourceAsStream(filename)) {
            if (is == null) {
                throw new RuntimeException("No se encontró " + filename + " en el classpath");
            }
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo " + filename, e);
        }
        return props;
    }

    /**
     * Ejecuta un script SQL desde el classpath.
     * Divide por ";" e ignora líneas de comentario.
     * Usa IF NOT EXISTS en cada CREATE TABLE → idempotente en cada arranque.
     */
    private static void runDdlScript(String scriptName) {
        log.info("Ejecutando DDL desde classpath:{}", scriptName);
        try (InputStream is = DatabaseConfig.class
                .getClassLoader().getResourceAsStream(scriptName)) {
            if (is == null) {
                log.warn("Script DDL '{}' no encontrado en classpath — se omite", scriptName);
                return;
            }
            String sql = new String(is.readAllBytes());
            String[] statements = sql.split(";");

            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                int executed = 0;
                for (String raw : statements) {
                    String s = raw.strip();
                    // Ignorar comentarios y vacíos
                    if (s.isEmpty() || s.startsWith("--")) continue;
                    // Filtrar bloques que son solo comentarios
                    String noComments = Arrays.stream(s.split("\n"))
                            .filter(line -> !line.strip().startsWith("--"))
                            .reduce("", (a, b) -> a + "\n" + b).strip();
                    if (noComments.isEmpty()) continue;

                    try {
                        stmt.execute(noComments);
                        executed++;
                    } catch (SQLException e) {
                        // Ignorar errores de duplicado (tabla ya existe, clave duplicada)
                        String msg = e.getMessage();
                        if (msg != null && (msg.contains("already exists")
                                || msg.contains("Duplicate") || msg.contains("23"))) {
                            log.debug("DDL omitido (ya existe): {}", noComments.substring(0, Math.min(60, noComments.length())));
                        } else {
                            log.error("Error ejecutando DDL: {}", noComments.substring(0, Math.min(80, noComments.length())), e);
                        }
                    }
                }
                log.info("DDL completado — {} sentencias ejecutadas", executed);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error ejecutando script DDL: " + scriptName, e);
        }
    }
}
