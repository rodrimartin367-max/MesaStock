package es.restaurante.inventario.controller;

import es.restaurante.inventario.config.AppContextListener;
import es.restaurante.inventario.dto.CategoriaRequestDto;
import es.restaurante.inventario.service.CategoriaService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/categorias/*")
public final class CategoriaServlet extends BaseApiServlet {

    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta) throws IOException {
        ejecutar(respuesta, () -> {
            CategoriaService servicio = servicio(AppContextListener.CATEGORIA_SERVICE_ATTRIBUTE, CategoriaService.class);
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
            CategoriaRequestDto dto = leerJson(solicitud, CategoriaRequestDto.class);
            return json(HttpServletResponse.SC_CREATED,
                    servicio(AppContextListener.CATEGORIA_SERVICE_ATTRIBUTE, CategoriaService.class).crear(dto));
        });
    }

    @Override
    protected void doPut(HttpServletRequest solicitud, HttpServletResponse respuesta) throws IOException {
        ejecutar(respuesta, () -> {
            Long id = idDeRuta(solicitud.getPathInfo());
            CategoriaRequestDto dto = leerJson(solicitud, CategoriaRequestDto.class);
            return json(HttpServletResponse.SC_OK,
                    servicio(AppContextListener.CATEGORIA_SERVICE_ATTRIBUTE, CategoriaService.class).actualizar(id, dto));
        });
    }

    @Override
    protected void doDelete(HttpServletRequest solicitud, HttpServletResponse respuesta) throws IOException {
        ejecutar(respuesta, () -> {
            Long id = idDeRuta(solicitud.getPathInfo());
            servicio(AppContextListener.CATEGORIA_SERVICE_ATTRIBUTE, CategoriaService.class).eliminar(id);
            return sinContenido();
        });
    }
}