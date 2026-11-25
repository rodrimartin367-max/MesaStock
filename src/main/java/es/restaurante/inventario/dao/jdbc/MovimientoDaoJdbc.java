package es.restaurante.inventario.dao.jdbc;

import es.restaurante.inventario.dao.MovimientoDao;
import es.restaurante.inventario.model.MovimientoStock;
import es.restaurante.inventario.model.TipoMovimiento;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MovimientoDaoJdbc implements MovimientoDao {

    private final DataSource dataSource;

    public MovimientoDaoJdbc(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    @Override
    public List<MovimientoStock> findAll(Long productoId) {
        StringBuilder sql = new StringBuilder("SELECT m.id, m.producto_id, p.nombre AS producto_nombre, "
                + "m.tipo, m.cantidad, m.fecha, m.nota FROM movimiento_stock m "
                + "JOIN producto p ON m.producto_id = p.id");
        if (productoId != null) {
            sql.append(" WHERE m.producto_id = ?");
        }
        sql.append(" ORDER BY m.fecha DESC");

        List<MovimientoStock> movimientos = new ArrayList<>();
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql.toString())) {
            if (productoId != null) {
                sentencia.setLong(1, productoId);
            }
            try (ResultSet resultados = sentencia.executeQuery()) {
                while (resultados.next()) {
                    movimientos.add(mapear(resultados));
                }
            }
            return movimientos;
        } catch (SQLException excepcion) {
            throw JdbcExceptionMapper.convertir(excepcion, "No se pudo consultar el movimiento",
                    "Error al listar los movimientos");
        }
    }

    @Override
    public long insert(Connection conexion, MovimientoStock movimiento) {
        String sql = "INSERT INTO movimiento_stock (producto_id, tipo, cantidad, nota) VALUES (?, ?, ?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setLong(1, movimiento.getProductoId());
            sentencia.setString(2, movimiento.getTipo().name());
            sentencia.setBigDecimal(3, movimiento.getCantidad());
            sentencia.setString(4, movimiento.getNota());
            sentencia.executeUpdate();
            long id = JdbcDaoUtils.obtenerIdGenerado(sentencia, "el movimiento");
            movimiento.setId(id);
            movimiento.setFecha(obtenerFecha(conexion, id));
            return id;
        } catch (SQLException excepcion) {
            throw JdbcExceptionMapper.convertir(excepcion,
                    "El movimiento no es válido o el producto no existe", "Error al registrar el movimiento");
        }
    }

    private static LocalDateTime obtenerFecha(Connection conexion, long id) throws SQLException {
        String sql = "SELECT fecha FROM movimiento_stock WHERE id = ?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    Timestamp fecha = resultado.getTimestamp("fecha");
                    return fecha == null ? null : fecha.toLocalDateTime();
                }
            }
        }
        return null;
    }

    private static MovimientoStock mapear(ResultSet resultado) throws SQLException {
        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setId(resultado.getLong("id"));
        movimiento.setProductoId(resultado.getLong("producto_id"));
        movimiento.setProductoNombre(resultado.getString("producto_nombre"));
        movimiento.setTipo(TipoMovimiento.valueOf(resultado.getString("tipo")));
        movimiento.setCantidad(resultado.getBigDecimal("cantidad"));
        Timestamp fecha = resultado.getTimestamp("fecha");
        movimiento.setFecha(fecha == null ? null : fecha.toLocalDateTime());
        movimiento.setNota(resultado.getString("nota"));
        return movimiento;
    }
}