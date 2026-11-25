package es.restaurante.inventario.service;

import es.restaurante.inventario.dao.CategoriaDao;
import es.restaurante.inventario.dao.ProductoDao;
import es.restaurante.inventario.dao.ProveedorDao;
import es.restaurante.inventario.model.Categoria;
import es.restaurante.inventario.model.Producto;
import es.restaurante.inventario.model.Proveedor;
import es.restaurante.inventario.model.UnidadMedida;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ProductoDao productoDao;
    @Mock
    private CategoriaDao categoriaDao;
    @Mock
    private ProveedorDao proveedorDao;

    private DashboardService servicio;

    @BeforeEach
    void setUp() {
        servicio = new DashboardService(productoDao, categoriaDao, proveedorDao);
    }

    @Test
    void calculaTotalesValorDeInventarioYAlertas() {
        Producto productoBajoStock = producto(1L, "Leche", "2.00", "3.00", "1.50");
        Producto productoNormal = producto(2L, "Arroz", "5.00", "2.00", "2.00");
        when(productoDao.findAll(null, false)).thenReturn(List.of(productoBajoStock, productoNormal));
        when(categoriaDao.findAll()).thenReturn(List.of(new Categoria(), new Categoria()));
        when(proveedorDao.findAll()).thenReturn(List.of(new Proveedor()));

        var dashboard = servicio.obtener();

        assertEquals(2L, dashboard.totalProductos());
        assertEquals(2L, dashboard.totalCategorias());
        assertEquals(1L, dashboard.totalProveedores());
        assertEquals(0, new BigDecimal("13.00").compareTo(dashboard.valorInventario()));
        assertEquals(1, dashboard.productosBajoStock().size());
        assertEquals(1L, dashboard.productosBajoStock().getFirst().id());
    }

    private static Producto producto(Long id, String nombre, String stock, String minimo, String precio) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre(nombre);
        producto.setUnidadMedida(UnidadMedida.UD);
        producto.setStockActual(new BigDecimal(stock));
        producto.setStockMinimo(new BigDecimal(minimo));
        producto.setPrecioUnitario(new BigDecimal(precio));
        return producto;
    }
}