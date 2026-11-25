package es.restaurante.inventario.controller;

import es.restaurante.inventario.config.AppContextListener;
import es.restaurante.inventario.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServletTest extends ServletTestSupport {

    @Mock
    private ProductoService servicio;

    private ProductoServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        when(contexto.getAttribute(AppContextListener.PRODUCTO_SERVICE_ATTRIBUTE)).thenReturn(servicio);
        servlet = new ProductoServlet();
        inicializar(servlet);
    }

    @Test
    void getAplicaFiltrosDeCategoriaYStockBajo() throws Exception {
        when(solicitud.getPathInfo()).thenReturn(null);
        when(solicitud.getParameter("categoriaId")).thenReturn("5");
        when(solicitud.getParameter("bajoStock")).thenReturn("true");
        when(servicio.listar(5L, true)).thenReturn(List.of());

        servlet.doGet(solicitud, respuesta);

        verify(servicio).listar(5L, true);
        verify(respuesta).setStatus(200);
    }

    @Test
    void deleteProveedorDelProductoDevuelve204() throws Exception {
        when(solicitud.getPathInfo()).thenReturn("/7/proveedores/4");

        servlet.doDelete(solicitud, respuesta);

        verify(servicio).eliminarProveedor(7L, 4L);
        verify(respuesta).setStatus(204);
    }

    @Test
    void filtroBooleanoInvalidoDevuelve400() throws Exception {
        when(solicitud.getPathInfo()).thenReturn(null);
        when(solicitud.getParameter("bajoStock")).thenReturn("si");

        servlet.doGet(solicitud, respuesta);

        verify(respuesta).setStatus(400);
        verify(servicio, org.mockito.Mockito.never()).listar(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyBoolean());
    }
}