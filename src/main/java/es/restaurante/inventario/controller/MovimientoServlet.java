package es.restaurante.inventario.controller;

import es.restaurante.inventario.config.AppContextListener;
import es.restaurante.inventario.dto.MovimientoRequestDto;
import es.restaurante.inventario.service.MovimientoService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/movimientos/*")
public final class MovimientoServlet extends BaseApiServlet {

    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta) throws IOException {
        ejecutar(respuesta, () -> {
            if (!segmentos(solicitud.getPathInfo()).isEmpty()) {
                throw rutaNoEncontrada();
            }
            String filtro = solicitud.getParameter("productoId");
            Long productoId = filtro == null || filtro.isBlank() ? null : parsearId(filtro, "productoId");
            return json(HttpServletResponse.SC_OK,
                    servicio(AppContextListener.MOVIMIENTO_SERVICE_ATTRIBUTE, MovimientoService.class).listar(productoId));
        });
    }

    @Override
    protected void doPost(HttpServletRequest solicitud, HttpServletResponse respuesta) throws IOException {
        ejecutar(respuesta, () -> {
            if (!segmentos(solicitud.getPathInfo()).isEmpty()) {
                throw rutaNoEncontrada();
            }
            MovimientoRequestDto dto = leerJson(solicitud, MovimientoRequestDto.class);
            return json(HttpServletResponse.SC_CREATED,
                    servicio(AppContextListener.MOVIMIENTO_SERVICE_ATTRIBUTE, MovimientoService.class).registrar(dto));
        });
    }
}