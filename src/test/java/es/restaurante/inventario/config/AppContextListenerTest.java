package es.restaurante.inventario.config;

import es.restaurante.inventario.service.CategoriaService;
import es.restaurante.inventario.service.DashboardService;
import es.restaurante.inventario.service.MovimientoService;
import es.restaurante.inventario.service.ProductoService;
import es.restaurante.inventario.service.ProveedorService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import javax.sql.DataSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.isA;
import static org.mockito.ArgumentMatchers.eq;

class AppContextListenerTest {

    @Test
    void compruebaLaConexionYPublicaLosServiciosAlIniciarElContexto() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection conexion = mock(Connection.class);
        ServletContext contexto = mock(ServletContext.class);
        when(dataSource.getConnection()).thenReturn(conexion);
        when(conexion.isValid(2)).thenReturn(true);
        ServletContextEvent evento = new ServletContextEvent(contexto);
        AppContextListener listener = new AppContextListener(new DataSourceProvider(dataSource));

        listener.contextInitialized(evento);

        verify(contexto).setAttribute(AppContextListener.DATA_SOURCE_ATTRIBUTE, dataSource);
        verify(contexto).setAttribute(eq(AppContextListener.CATEGORIA_SERVICE_ATTRIBUTE), isA(CategoriaService.class));
        verify(contexto).setAttribute(eq(AppContextListener.PROVEEDOR_SERVICE_ATTRIBUTE), isA(ProveedorService.class));
        verify(contexto).setAttribute(eq(AppContextListener.PRODUCTO_SERVICE_ATTRIBUTE), isA(ProductoService.class));
        verify(contexto).setAttribute(eq(AppContextListener.MOVIMIENTO_SERVICE_ATTRIBUTE), isA(MovimientoService.class));
        verify(contexto).setAttribute(eq(AppContextListener.DASHBOARD_SERVICE_ATTRIBUTE), isA(DashboardService.class));
        verify(conexion).close();
    }
}