package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VentaRepository extends AbstractJpaRepository<Venta, Long> {
    @Override protected String getTableName() { return "upeu_venta"; }
    @Override protected String getPkColumn()  { return "id_venta"; }

    @Override
    protected Venta mapRow(ResultSet rs) throws SQLException {
        // Construye cliente con nombre si la columna existe (viene del JOIN en findById)
        Cliente.ClienteBuilder cliBuilder = Cliente.builder().dniruc(rs.getString("dniruc"));
        try { cliBuilder.nombres(rs.getString("nombres")); } catch (SQLException ignored) {}
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getLong("id_usuario"));
        return Venta.builder()
                .idVenta(rs.getLong("id_venta")).preciobase(rs.getDouble("preciobase"))
                .igv(rs.getDouble("igv")).preciototal(rs.getDouble("preciototal"))
                .dniruc(cliBuilder.build())
                .idUsuario(usuario)
                .numDoc(rs.getString("num_doc"))
                .fechaGener(rs.getTimestamp("fecha_gener").toLocalDateTime())
                .serie(rs.getString("serie")).tipoDoc(rs.getString("tipo_doc"))
                .build();
    }

    @Override
    protected Venta insert(Connection conn, Venta e) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO upeu_venta(preciobase,igv,preciototal,dniruc,id_usuario,num_doc,fecha_gener,serie,tipo_doc) VALUES(?,?,?,?,?,?,?,?,?)",
                e.getPreciobase(), e.getIgv(), e.getPreciototal(),
                e.getDniruc().getDniruc(), e.getIdUsuario().getIdUsuario(),
                e.getNumDoc(), Timestamp.valueOf(e.getFechaGener()),
                e.getSerie(), e.getTipoDoc());
        e.setIdVenta(id);
        return e;
    }

    @Override
    protected Venta updateRow(Connection conn, Venta e) throws SQLException {
        executeUpdate(conn,
                "UPDATE upeu_venta SET preciobase=?,igv=?,preciototal=?,dniruc=?,id_usuario=?,num_doc=?,fecha_gener=?,serie=?,tipo_doc=? WHERE id_venta=?",
                e.getPreciobase(), e.getIgv(), e.getPreciototal(),
                e.getDniruc().getDniruc(), e.getIdUsuario().getIdUsuario(),
                e.getNumDoc(), Timestamp.valueOf(e.getFechaGener()),
                e.getSerie(), e.getTipoDoc(), e.getIdVenta());
        return e;
    }

    /**
     * findById con JOIN a cliente + eager-load de detalles.
     * Necesario para JasperReports y impresión ESC/POS.
     */
    @Override
    public Optional<Venta> findById(Long id) {
        Optional<Venta> opt = executeQueryOne(
                "SELECT v.*, c.nombres FROM upeu_venta v " +
                "JOIN upeu_cliente c ON v.dniruc = c.dniruc WHERE v.id_venta = ?", id);
        opt.ifPresent(v -> v.setDetalleVenta(loadDetalles(id)));
        return opt;
    }

    private List<VentaDetalle> loadDetalles(Long idVenta) {
        List<VentaDetalle> detalles = new ArrayList<>();
        String sql = "SELECT vd.*, p.nombre AS prod_nombre FROM upeu_venta_detalle vd " +
                     "JOIN upeu_producto p ON vd.id_producto = p.id_producto WHERE vd.id_venta = ?";
        try (Connection conn = openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    detalles.add(VentaDetalle.builder()
                            .idVentaDetalle(rs.getLong("id_venta_detalle"))
                            .pu(rs.getDouble("pu")).cantidad(rs.getDouble("cantidad"))
                            .descuento(rs.getDouble("descuento")).subtotal(rs.getDouble("subtotal"))
                            .idProducto(Producto.builder().idProducto(rs.getLong("id_producto"))
                                    .nombre(rs.getString("prod_nombre")).build())
                            .build());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error cargando detalles de venta", e);
        }
        return detalles;
    }
}
