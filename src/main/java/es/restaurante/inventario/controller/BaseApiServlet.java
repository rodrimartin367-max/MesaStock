package es.restaurante.inventario.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import es.restaurante.inventario.config.JsonMapper;
import es.restaurante.inventario.exception.ConflictoException;
import es.restaurante.inventario.exception.DataAccessException;
import es.restaurante.inventario.exception.RecursoNoEncontradoException;
import es.restaurante.inventario.exception.ValidacionException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseApiServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(BaseApiServlet.class.getName());

    protected final <T> T servicio(String atributo, Class<T> tipo) {
        Object servicio = getServletContext().getAttribute(atributo);
        if (!tipo.isInstance(servicio)) {
            throw new IllegalStateException("No está disponible el servicio requerido: " + atributo);
        }
        return tipo.cast(servicio);
    }

    protected final <T> T leerJson(HttpServletRequest solicitud, Class<T> tipo) {
        try {
            return JsonMapper.getInstance().readValue(solicitud.getInputStream(), tipo);
        } catch (JsonProcessingException excepcion) {
            throw new JsonMalformadoException(excepcion);
        } catch (IOException excepcion) {
            throw new UncheckedIOException(excepcion);
        }
    }

    protected final Long parsearId(String valor, String campo) {
        if (valor == null || !valor.matches("[0-9]+")) {
            throw new ValidacionException("El id debe ser un número válido", Map.of(campo, "Debe ser un número entero."));
        }
        try {
            return Long.parseLong(valor);
        } catch (NumberFormatException excepcion) {
            throw new ValidacionException("El id debe ser un número válido", Map.of(campo, "Está fuera del rango permitido."));
        }
    }

    protected final Long idDeRuta(String pathInfo) {
        List<String> segmentos = segmentos(pathInfo);
        if (segmentos.size() != 1) {
            throw rutaNoEncontrada();
        }
        return parsearId(segmentos.getFirst(), "id");
    }

    protected final List<String> segmentos(String pathInfo) {
        if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
            return List.of();
        }
        if (!pathInfo.startsWith("/") || pathInfo.endsWith("/")) {
            throw rutaNoEncontrada();
        }
        String[] segmentos = pathInfo.substring(1).split("/", -1);
        for (String segmento : segmentos) {
            if (segmento.isEmpty()) {
                throw rutaNoEncontrada();
            }
        }
        return List.of(segmentos);
    }

    protected final RecursoNoEncontradoException rutaNoEncontrada() {
        return new RecursoNoEncontradoException("La ruta solicitada no existe");
    }

    protected final Respuesta json(int estado, Object cuerpo) {
        return new Respuesta(estado, cuerpo);
    }

    protected final Respuesta sinContenido() {
        return new Respuesta(HttpServletResponse.SC_NO_CONTENT, null);
    }

    protected final void ejecutar(HttpServletResponse respuesta, Supplier<Respuesta> operacion) throws IOException {
        Respuesta resultado;
        try {
            resultado = operacion.get();
        } catch (ValidacionException excepcion) {
            resultado = error(HttpServletResponse.SC_BAD_REQUEST, "Bad Request", excepcion.getMessage(), excepcion.getErrores());
        } catch (JsonMalformadoException excepcion) {
            resultado = error(HttpServletResponse.SC_BAD_REQUEST, "Bad Request", "El cuerpo JSON no es válido", Map.of());
        } catch (RecursoNoEncontradoException excepcion) {
            resultado = error(HttpServletResponse.SC_NOT_FOUND, "Not Found", excepcion.getMessage(), Map.of());
        } catch (ConflictoException excepcion) {
            resultado = error(HttpServletResponse.SC_CONFLICT, "Conflict", excepcion.getMessage(), Map.of());
        } catch (DataAccessException excepcion) {
            LOGGER.log(Level.SEVERE, "Error de acceso a datos al procesar la API REST", excepcion);
            resultado = errorInterno();
        } catch (RuntimeException excepcion) {
            LOGGER.log(Level.SEVERE, "Error inesperado al procesar la API REST", excepcion);
            resultado = errorInterno();
        }

        respuesta.setStatus(resultado.estado());
        if (resultado.estado() == HttpServletResponse.SC_NO_CONTENT) {
            return;
        }
        respuesta.setCharacterEncoding("UTF-8");
        respuesta.setContentType("application/json; charset=UTF-8");
        JsonMapper.getInstance().writeValue(respuesta.getWriter(), resultado.cuerpo());
    }

    private static Respuesta error(int estado, String nombre, String mensaje, Map<String, String> detalles) {
        return new Respuesta(estado, new ErrorApi(estado, nombre, mensaje, detalles));
    }

    private static Respuesta errorInterno() {
        return error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error",
                "Error interno del servidor", Collections.emptyMap());
    }

    protected record Respuesta(int estado, Object cuerpo) {
    }

    private record ErrorApi(int status, String error, String mensaje, Map<String, String> detalles) {
    }

    private static final class JsonMalformadoException extends RuntimeException {

        private JsonMalformadoException(Throwable causa) {
            super(causa);
        }
    }
}