package es.restaurante.inventario.dao.jdbc;

import es.restaurante.inventario.dao.ProductoDao;
import es.restaurante.inventario.exception.ConflictoException;
import es.restaurante.inventario.model.MovimientoStock;
import es.restaurante.inventario.model.Producto;
import es.restaurante.inventario.model.ProductoProveedor;
import es.restaurante.inventario.model.TipoMovimiento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductoDaoJdbcTest extends JdbcDaoTestSupport {

    private final ProductoDao dao = new ProductoDaoJdbc(DATA_SOURCE);

    @Test
    void realizaCrudFiltraPorCategoriaYStockBajo() {
        long categoriaId = insertarCategoria("Carnes");
        long otraCategoriaId = insertarCategoria("Pescados");
        Producto bajoStock = nuevoProducto(categoriaId, "Ternera", "2.00", "5.00");
        long bajoStockId = dao.insert(bajoStock);
        insertarProducto(categoriaId, "Pollo", "8.00", "5.00");
        insertarProducto(otraCategoriaId, "Salmón", "1.00", "3.00");

        assertEquals(3, dao.findAll(null, false).size());
        assertEquals(1, dao.findAll(categoriaId, true).size());
        assertEquals(bajoStockId, dao.findAll(categoriaId, true).getFirst().getId());
        assertEquals(1, dao.findAll(otraCategoriaId, true).size());
        assertTrue(dao.findById(-1L).isEmpty());
        assertEquals("Carnes", dao.findById(bajoStockId).orElseThrow().getCategoriaNombre());

        bajoStock.setNombre("Ternera roja");
        bajoStock.setStockActual(new BigDecimal("99.00"));
        assertTrue(dao.update(bajoStock));
        assertEquals("Ternera roja", dao.findById(bajoStockId).orElseThrow().getNombre());
        assertEquals(new BigDecimal("2.00"), dao.findById(bajoStockId).orElseThrow().getStockActual());
        assertTrue(dao.delete(bajoStockId));
        assertFalse(dao.delete(bajoStockId));
    }

    @Test
    void bloqueaYActualizaStockSinCerrarLaConexionDelServicio() throws Exception {
        long categoriaId = insertarCategoria("Lácteos");
        long productoId = insertarProducto(categoriaId, "Leche", "5.00", "2.00");

        try (Connection conexion = DATA_SOURCE.getConnection()) {
            conexion.setAutoCommit(false);
            assertTrue(dao.findByIdForUpdate(conexion, productoId).isPresent());
            assertFalse(conexion.isClosed());
            assertTrue(dao.updateStock(conexion, productoId, new BigDecimal("7.50")));
            assertFalse(conexion.isClosed());
            conexion.commit();
        }

        assertEquals(new BigDecimal("7.50"), dao.findById(productoId).orElseThrow().getStockActual());
    }

    @Test
    void borrarProductoEliminaEnCascadaMovimientosYProveedores() throws Exception {
        long categoriaId = insertarCategoria("Frutas");
        long productoId = insertarProducto(categoriaId, "Manzana", "2.00", "1.00");
        long proveedorId = insertarProveedor("Frutas del Valle", "B1234567X");
        ProductoProveedorDaoJdbc relacionDao = new ProductoProveedorDaoJdbc(DATA_SOURCE);
        ProductoProveedor relacion = new ProductoProveedor();
        relacion.setProductoId(productoId);
        relacion.setProveedorId(proveedorId);
        relacion.setPrecioCompra(new BigDecimal("1.20"));
        relacionDao.insert(relacion);
        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setProductoId(productoId);
        movimiento.setTipo(TipoMovimiento.ENTRADA);
        movimiento.setCantidad(new BigDecimal("2.00"));
        try (Connection conexion = DATA_SOURCE.getConnection()) {
            new MovimientoDaoJdbc(DATA_SOURCE).insert(conexion, movimiento);
        }

        assertTrue(dao.delete(productoId));

        assertTrue(relacionDao.findByProducto(productoId).isEmpty());
        assertTrue(new MovimientoDaoJdbc(DATA_SOURCE).findAll(productoId).isEmpty());
    }

    @Test
    void convierteProductoDuplicadoEnConflicto() {
        long categoriaId = insertarCategoria("Panadería");
        dao.insert(nuevoProducto(categoriaId, "Harina", "3.00", "1.00"));

        assertThrows(ConflictoException.class,
                () -> dao.insert(nuevoProducto(categoriaId, "Harina", "4.00", "1.00")));
    }
}