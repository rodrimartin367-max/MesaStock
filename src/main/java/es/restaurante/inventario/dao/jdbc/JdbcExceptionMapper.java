package es.restaurante.inventario.dao.jdbc;

import es.restaurante.inventario.exception.ConflictoException;
import es.restaurante.inventario.exception.DataAccessException;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

final class JdbcExceptionMapper {

    private JdbcExceptionMapper() {
    }

    static RuntimeException convertir(SQLException excepcion, String mensajeConflicto, String mensajeDatos) {
        String estadoSql = excepcion.getSQLState();
        if (excepcion instanceof SQLIntegrityConstraintViolationException
                || estadoSql != null && estadoSql.startsWith("23")) {
            return new ConflictoException(mensajeConflicto);
        }
        return new DataAccessException(mensajeDatos, excepcion);
    }
}