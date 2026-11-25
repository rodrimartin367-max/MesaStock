package es.restaurante.inventario.dao.jdbc;

import es.restaurante.inventario.exception.ConflictoException;
import es.restaurante.inventario.model.ProductoProveedor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductoProveedorDaoJdbcTest extends JdbcDaoTestSupport {

    private final ProductoProveedorDaoJdbc dao = new ProductoProveedorDaoJdbc(DATA_SOURCE);

    @Test
    void insertaConsultaYEliminaRelacionConNombreDelProveedor() {
        long categoriaId = insertarCategoria("Bebidas");
        long productoId = insertarProducto(categoriaId, "Agua", "10.00", "2.00");
        long proveedorId = insertarProveedor("Aguas del Norte", "B1234567X");
        ProductoProveedor relacion = nuevaRelacion(productoId, proveedorId);

        dao.insert(relacion);

        ProductoProveedor recuperada = dao.findByProducto(productoId).getFirst();
        assertEquals("Aguas del Norte", recuperada.getNombreProveedor());
        assertEquals(new BigDecimal("1.25"), recuperada.getPrecioCompra());
        assertTrue(dao.delete(productoId, proveedorId));
        assertFalse(dao.delete(productoId, proveedorId));
        assertTrue(dao.findByProducto(productoId).isEmpty());
    }

    @Test
    void claveCompuestaDuplicadaSeTraduceAConflicto() {
        long categoriaId = insertarCategoria("Conservas");
        long productoId = insertarProducto(categoriaId, "Atún", "5.00", "2.00");
        long proveedorId = insertarProveedor("Conservas Sur", "B1234567X");
        dao.insert(nuevaRelacion(productoId, proveedorId));

        assertThrows(ConflictoException.class, () -> dao.insert(nuevaRelacion(productoId, proveedorId)));
    }

    private static ProductoProveedor nuevaRelacion(long productoId, long proveedorId) {
        ProductoProveedor relacion = new ProductoProveedor();
        relacion.setProductoId(productoId);
        relacion.setProveedorId(proveedorId);
        relacion.setPrecioCompra(new BigDecimal("1.25"));
        return relacion;
    }
}