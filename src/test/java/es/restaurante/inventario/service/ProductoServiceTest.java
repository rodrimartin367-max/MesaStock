package es.restaurante.inventario.service;

import es.restaurante.inventario.dao.CategoriaDao;
import es.restaurante.inventario.dao.ProductoDao;
import es.restaurante.inventario.dao.ProductoProveedorDao;
import es.restaurante.inventario.dao.ProveedorDao;
import es.restaurante.inventario.dto.ProductoProveedorRequestDto;
import es.restaurante.inventario.dto.ProductoRequestDto;
import es.restaurante.inventario.exception.RecursoNoEncontradoException;
import es.restaurante.inventario.exception.ValidacionException;
import es.restaurante.inventario.model.Categoria;
import es.restaurante.inventario.model.Producto;
import es.restaurante.inventario.model.ProductoProveedor;
import es.restaurante.inventario.model.Proveedor;
import es.restaurante.inventario.model.UnidadMedida;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoDao productoDao;
    @Mock
    private CategoriaDao categoriaDao;
    @Mock
    private ProveedorDao proveedorDao;
    @Mock
    private ProductoProveedorDao productoProveedorDao;

    private ProductoService servicio;

    @BeforeEach
    void setUp() {
        servicio = new ProductoService(productoDao, categoriaDao, proveedorDao, productoProveedorDao);
    }

    @Test
    void creaActualizaListaYBorraProductoSinEditarElStock() {
        when(categoriaDao.findById(2L)).thenReturn(Optional.of(categoria(2L)));
        when(productoDao.insert(any(Producto.class))).thenReturn(7L);
        when(productoDao.findAll(2L, false)).thenReturn(List.of(producto(7L, "5.00")));
        when(productoDao.findById(7L)).thenReturn(Optional.of(producto(7L, "5.00")));
        when(productoDao.update(any(Producto.class))).thenReturn(true);
        when(productoDao.delete(7L)).thenReturn(true);

        var request = new ProductoRequestDto(" Tomate ", 2L, UnidadMedida.KG,
                new BigDecimal("1.00"), new BigDecimal("2.50"));
        assertEquals("Tomate", servicio.crear(request).nombre());
        assertEquals(1, servicio.listar(2L, false).size());
        assertEquals("Tomate", servicio.actualizar(7L, request).nombre());
        servicio.eliminar(7L);

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(productoDao).update(captor.capture());
        assertEquals(new BigDecimal("5.00"), captor.getValue().getStockActual());
        verify(productoDao).delete(7L);
    }

    @Test
    void validaLosCamposAntesDeCrear() {
        ValidacionException excepcion = assertThrows(ValidacionException.class,
                () -> servicio.crear(new ProductoRequestDto(" ", null, null, new BigDecimal("-1"), null)));

        assertEquals(5, excepcion.getErrores().size());
        verify(productoDao, never()).insert(any());
        verify(categoriaDao, never()).findById(any());
    }

    @Test
    void requiereProductoYProveedorParaGestionarLaRelacion() {
        when(productoDao.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.listarProveedores(99L));

        when(productoDao.findById(1L)).thenReturn(Optional.of(producto(1L, "0.00")));
        when(proveedorDao.findById(2L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.agregarProveedor(1L,
                new ProductoProveedorRequestDto(2L, BigDecimal.ONE)));
    }

    @Test
    void listaYAsociaProveedoresAlProducto() {
        when(productoDao.findById(1L)).thenReturn(Optional.of(producto(1L, "0.00")));
        when(proveedorDao.findById(2L)).thenReturn(Optional.of(new Proveedor()));
        when(productoProveedorDao.findByProducto(1L)).thenReturn(List.of(new ProductoProveedor()));
        when(productoProveedorDao.delete(1L, 2L)).thenReturn(true);

        assertEquals(1, servicio.listarProveedores(1L).size());
        assertEquals(1L, servicio.agregarProveedor(1L,
                new ProductoProveedorRequestDto(2L, new BigDecimal("3.20"))).productoId());
        servicio.eliminarProveedor(1L, 2L);
        verify(productoProveedorDao).insert(any(ProductoProveedor.class));
        verify(productoProveedorDao).delete(1L, 2L);
    }

    private static Categoria categoria(Long id) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNombre("Verduras");
        return categoria;
    }

    private static Producto producto(Long id, String stock) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre("Tomate");
        producto.setCategoriaId(2L);
        producto.setCategoriaNombre("Verduras");
        producto.setUnidadMedida(UnidadMedida.KG);
        producto.setStockActual(new BigDecimal(stock));
        producto.setStockMinimo(BigDecimal.ONE);
        producto.setPrecioUnitario(new BigDecimal("2.50"));
        return producto;
    }
}