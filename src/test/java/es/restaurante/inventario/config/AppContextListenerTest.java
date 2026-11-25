package es.restaurante.inventario.config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import javax.sql.DataSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppContextListenerTest {

    @Test
    void compruebaYPublicaElDataSourceAlIniciarElContexto() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection conexion = mock(Connection.class);
        ServletContext contexto = mock(ServletContext.class);
        when(dataSource.getConnection()).thenReturn(conexion);
        when(conexion.isValid(2)).thenReturn(true);
        ServletContextEvent evento = new ServletContextEvent(contexto);
        AppContextListener listener = new AppContextListener(new DataSourceProvider(dataSource));

        listener.contextInitialized(evento);

        verify(contexto).setAttribute(AppContextListener.DATA_SOURCE_ATTRIBUTE, dataSource);
        verify(conexion).close();
    }
}