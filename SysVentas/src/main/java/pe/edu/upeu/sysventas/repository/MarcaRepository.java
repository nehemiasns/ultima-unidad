package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.model.Marca;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MarcaRepository extends AbstractJpaRepository<Marca, Long> {
    @Override protected String getTableName() { return "upeu_marca"; }
    @Override protected String getPkColumn()  { return "id_marca"; }

    @Override
    protected Marca mapRow(ResultSet rs) throws SQLException {

        return Marca.builder()
                .idMarca(rs.getLong("id_marca"))
                .nombre(rs.getString("nombre"))
                .build();
    }

    @Override
    protected Marca insert(Connection conn, Marca e) throws SQLException {
        long id = executeInsertGetKey(conn, "INSERT INTO upeu_marca(nombre) VALUES(?)",
                e.getNombre());
        e.setIdMarca(id); return e;
    }

    @Override
    protected Marca updateRow(Connection conn, Marca e) throws SQLException {
        executeUpdate(conn, "UPDATE upeu_marca SET nombre=? WHERE id_marca=?",
                e.getNombre(),
                e.getIdMarca());
        return e;
    }
}
