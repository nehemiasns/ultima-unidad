package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.model.UnidMedida;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UnidadMedidaRepository extends AbstractJpaRepository<UnidMedida, Long> {
    @Override protected String getTableName() { return "upeu_unid_medida"; }
    @Override protected String getPkColumn()  { return "id_unidad"; }

    @Override
    protected UnidMedida mapRow(ResultSet rs) throws SQLException {
        return UnidMedida.builder().idUnidad(rs.getLong("id_unidad"))
                .nombreMedida(rs.getString("nombre_medida"))
                .build();
    }
    @Override
    protected UnidMedida insert(Connection conn, UnidMedida e) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO upeu_unid_medida(nombre_medida) VALUES(?)",
                e.getNombreMedida());
        e.setIdUnidad(id); return e;
    }
    @Override
    protected UnidMedida updateRow(Connection conn, UnidMedida e) throws SQLException {
        executeUpdate(conn, "UPDATE upeu_unid_medida SET nombre_medida=? WHERE id_unidad=?",
                e.getNombreMedida(),
                e.getIdUnidad()); return e;
    }
}
