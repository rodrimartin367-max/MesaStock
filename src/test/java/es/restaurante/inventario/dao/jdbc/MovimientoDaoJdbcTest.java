package es.restaurante.inventario.dao.jdbc;

import es.restaurante.inventario.exception.ConflictoException;
import es.restaurante.inventario.model.MovimientoStock;
import es.restaurante.inventario.model.TipoMovimiento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MovimientoDaoJdbcTest extends JdbcDaoTestSupport {

    private final MovimientoDaoJdbc dao = new MovimientoDaoJdbc(DATA_SOURCE);

    @Test
    void insertaConConexionExternaYListaFiltrandoPorProductoEnFechaDescendente() throws Exception {
        long categoriaId = insertarCategoria("Pescados");
        long productoUno = insertarProducto(categoriaId, "Merluza", "5.00", "2.00");
        long productoDos = insertarProducto(categoriaId, "Salmón", "3.00", "1.00");
        MovimientoStock movimientoAntiguo = nuevoMovimiento(productoUno, "2.00");
        MovimientoStock movimientoReciente = nuevoMovimiento(productoDos, "1.00");
        try (Connection conexion = DATA_SOURCE.getConnection()) {
            assertTrue(dao.insert(conexion, movimientoAntiguo) > 0);
            assertTrue(dao.insert(conexion, movimientoReciente) > 0);
            assertNotNull(movimientoAntiguo.getFecha());
            assertFalse(conexion.isClosed());
        }
        establecerFecha(movimientoAntiguo.getId(), LocalDateTime.of(2026, 1, 1, 10, 0));
        establecerFecha(movimientoReciente.getId(), LocalDateTime.of(2026, 1, 2, 10, 0));

        List<MovimientoStock> todos = dao.findAll(null);
        List<MovimientoStock> filtrados = dao.findAll(productoUno);

        assertEquals(movimientoReciente.getId(), todos.getFirst().getId());
        assertEquals(1, filtrados.size());
        assertEquals("Merluza", filtrados.getFirst().getProductoNombre());
    }

    @Test
    void movimientoConCantidadNoValidaSeTraduceAConflicto() throws Exception {
        long categoriaId = insertarCategoria("Verduras");
        long productoId = insertarProducto(categoriaId, "Zanahoria", "3.00", "1.00");
        MovimientoStock movimiento = nuevoMovimiento(productoId, "0.00");

        try (Connection conexion = DATA_SOURCE.getConnection()) {
            assertThrows(ConflictoException.class, () -> dao.insert(conexion, movimiento));
        }
    }

    private static MovimientoStock nuevoMovimiento(long productoId, String cantidad) {
        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setProductoId(productoId);
        movimiento.setTipo(TipoMovimiento.ENTRADA);
        movimiento.setCantidad(new BigDecimal(cantidad));
        movimiento.setNota("Prueba DAO");
        return movimiento;
    }

    private static void establecerFecha(long id, LocalDateTime fecha) throws Exception {
        String sql = "UPDATE movimiento_stock SET fecha = ? WHERE id = ?";
        try (Connection conexion = DATA_SOURCE.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setTimestamp(1, Timestamp.valueOf(fecha));
            sentencia.setLong(2, id);
            sentencia.executeUpdate();
        }
    }
}