package es.restaurante.inventario.controller;

import es.restaurante.inventario.config.AppContextListener;
import es.restaurante.inventario.dto.ProveedorRequestDto;
import es.restaurante.inventario.service.ProveedorService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/proveedores/*")
public final class ProveedorServlet extends BaseApiServlet {

    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta) throws IOException {
        ejecutar(respuesta, () -> {
            ProveedorService servicio = servicio(AppContextListener.PROVEEDOR_SERVICE_ATTRIBUTE, ProveedorService.class);
            String pathInfo = solicitud.getPathInfo();
            if (segmentos(pathInfo).isEmpty()) {
                return json(HttpServletResponse.SC_OK, servicio.listar());
            }
            return json(HttpServletResponse.SC_OK, servicio.obtenerPorId(idDeRuta(pathInfo)));
        });
    }

    @Override
    protected void doPost(HttpServletRequest solicitud, HttpServletResponse respuesta) throws IOException {
        ejecutar(respuesta, () -> {
            if (!segmentos(solicitud.getPathInfo()).isEmpty()) {
                throw rutaNoEncontrada();
            }
            ProveedorRequestDto dto = leerJson(solicitud, ProveedorRequestDto.class);
            return json(HttpServletResponse.SC_CREATED,
                    servicio(AppContextListener.PROVEEDOR_SERVICE_ATTRIBUTE, ProveedorService.class).crear(dto));
        });
    }

    @Override
    protected void doPut(HttpServletRequest solicitud, HttpServletResponse respuesta) throws IOException {
        ejecutar(respuesta, () -> {
            Long id = idDeRuta(solicitud.getPathInfo());
            ProveedorRequestDto dto = leerJson(solicitud, ProveedorRequestDto.class);
            return json(HttpServletResponse.SC_OK,
                    servicio(AppContextListener.PROVEEDOR_SERVICE_ATTRIBUTE, ProveedorService.class).actualizar(id, dto));
        });
    }

    @Override
    protected void doDelete(HttpServletRequest solicitud, HttpServletResponse respuesta) throws IOException {
        ejecutar(respuesta, () -> {
            Long id = idDeRuta(solicitud.getPathInfo());
            servicio(AppContextListener.PROVEEDOR_SERVICE_ATTRIBUTE, ProveedorService.class).eliminar(id);
            return sinContenido();
        });
    }
}