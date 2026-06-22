package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.model.Perfil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PerfilRepository extends AbstractJpaRepository<Perfil, Long> {
    @Override protected String getTableName() { return "upeu_perfil"; }
    @Override protected String getPkColumn()  { return "id_perfil"; }

    @Override
    protected Perfil mapRow(ResultSet rs) throws SQLException {
        return Perfil.builder().idPerfil(rs.getLong("id_perfil"))
                .nombre(rs.getString("nombre")).codigo(rs.getString("codigo")).build();
    }
    @Override
    protected Perfil insert(Connection conn, Perfil e) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO upeu_perfil(nombre,codigo) VALUES(?,?)", e.getNombre(), e.getCodigo());
        e.setIdPerfil(id); return e;
    }
    @Override
    protected Perfil updateRow(Connection conn, Perfil e) throws SQLException {
        executeUpdate(conn, "UPDATE upeu_perfil SET nombre=?,codigo=? WHERE id_perfil=?",
                e.getNombre(), e.getCodigo(), e.getIdPerfil()); return e;
    }
}
