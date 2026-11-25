package es.restaurante.inventario.controller;

import com.fasterxml.jackson.databind.JsonNode;
import es.restaurante.inventario.config.JsonMapper;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

abstract class ServletTestSupport {

    protected final ServletContext contexto = mock(ServletContext.class);
    protected final ServletConfig configuracion = mock(ServletConfig.class);
    protected final HttpServletRequest solicitud = mock(HttpServletRequest.class);
    protected final HttpServletResponse respuesta = mock(HttpServletResponse.class);
    private final StringWriter contenido = new StringWriter();
    private final PrintWriter escritor = new PrintWriter(contenido);

    @BeforeEach
    void prepararServlet() throws Exception {
        when(configuracion.getServletContext()).thenReturn(contexto);
        when(respuesta.getWriter()).thenReturn(escritor);
    }

    protected void inicializar(BaseApiServlet servlet) throws Exception {
        servlet.init(configuracion);
    }

    protected void body(String json) throws IOException {
        ByteArrayInputStream bytes = new ByteArrayInputStream(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        when(solicitud.getInputStream()).thenReturn(new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return bytes.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }

            @Override
            public int read() {
                return bytes.read();
            }
        });
    }

    protected JsonNode respuestaJson() throws IOException {
        escritor.flush();
        return JsonMapper.getInstance().readTree(contenido.toString());
    }
}