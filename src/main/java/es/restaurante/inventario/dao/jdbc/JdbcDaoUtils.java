package es.restaurante.inventario.dao.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

final class JdbcDaoUtils {

    private JdbcDaoUtils() {
    }

    static long obtenerIdGenerado(PreparedStatement sentencia, String entidad) throws SQLException {
        try (ResultSet claves = sentencia.getGeneratedKeys()) {
            if (claves.next()) {
                return claves.getLong(1);
            }
        }
        throw new SQLException("La base de datos no devolvió el id generado para " + entidad);
    }
}