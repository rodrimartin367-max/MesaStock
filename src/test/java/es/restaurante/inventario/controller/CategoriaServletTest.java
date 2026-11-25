package es.restaurante.inventario.controller;

import es.restaurante.inventario.config.AppContextListener;
import es.restaurante.inventario.dto.CategoriaDto;
import es.restaurante.inventario.dto.CategoriaRequestDto;
import es.restaurante.inventario.exception.ConflictoException;
import es.restaurante.inventario.exception.RecursoNoEncontradoException;
import es.restaurante.inventario.service.CategoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServletTest extends ServletTestSupport {

    @Mock
    private CategoriaService servicio;

    private CategoriaServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        when(contexto.getAttribute(AppContextListener.CATEGORIA_SERVICE_ATTRIBUTE)).thenReturn(servicio);
        servlet = new CategoriaServlet();
        inicializar(servlet);
    }

    @Test
    void postDevuelve201YElDtoCreado() throws Exception {
        when(solicitud.getPathInfo()).thenReturn(null);
        body("{\"nombre\":\"Bebidas\",\"descripcion\":\"Bebidas frías\"}");
        when(servicio.crear(any(CategoriaRequestDto.class)))
                .thenReturn(new CategoriaDto(3L, "Bebidas", "Bebidas frías"));

        servlet.doPost(solicitud, respuesta);

        verify(respuesta).setStatus(201);
        assertEquals(3, respuestaJson().get("id").asInt());
        assertEquals("Bebidas", respuestaJson().get("nombre").asText());
        verify(respuesta).setContentType("application/json; charset=UTF-8");
    }

    @Test
    void jsonMalformadoDevuelve400ConFormatoDeError() throws Exception {
        when(solicitud.getPathInfo()).thenReturn(null);
        body("{");

        servlet.doPost(solicitud, respuesta);

        verify(respuesta).setStatus(400);
        var error = respuestaJson();
        assertEquals(400, error.get("status").asInt());
        assertEquals("Bad Request", error.get("error").asText());
        assertEquals("El cuerpo JSON no es válido", error.get("mensaje").asText());
        verify(servicio, never()).crear(any());
    }

    @Test
    void idNoNumericoDevuelve400YRecursoAusenteDevuelve404() throws Exception {
        when(solicitud.getPathInfo()).thenReturn("/abc");

        servlet.doGet(solicitud, respuesta);

        verify(respuesta).setStatus(400);
        assertEquals("id", respuestaJson().get("detalles").fieldNames().next());
        verify(servicio, never()).obtenerPorId(any());

        when(solicitud.getPathInfo()).thenReturn("/88");
        when(servicio.obtenerPorId(88L)).thenThrow(new RecursoNoEncontradoException("No existe"));
        servlet.doGet(solicitud, respuesta);

        verify(respuesta).setStatus(404);
    }

    @Test
    void conflictoAlBorrarDevuelve409() throws Exception {
        when(solicitud.getPathInfo()).thenReturn("/3");
        org.mockito.Mockito.doThrow(new ConflictoException("La categoría tiene productos"))
                .when(servicio).eliminar(3L);

        servlet.doDelete(solicitud, respuesta);

        verify(respuesta).setStatus(409);
        assertEquals("La categoría tiene productos", respuestaJson().get("mensaje").asText());
    }
}