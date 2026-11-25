package es.restaurante.inventario.controller;

import es.restaurante.inventario.config.AppContextListener;
import es.restaurante.inventario.service.DashboardService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/dashboard")
public final class DashboardServlet extends BaseApiServlet {

    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta) throws IOException {
        ejecutar(respuesta, () -> {
            if (!segmentos(solicitud.getPathInfo()).isEmpty()) {
                throw rutaNoEncontrada();
            }
            return json(HttpServletResponse.SC_OK,
                    servicio(AppContextListener.DASHBOARD_SERVICE_ATTRIBUTE, DashboardService.class).obtener());
        });
    }
}