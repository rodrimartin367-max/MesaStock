package es.restaurante.inventario.controller;

import es.restaurante.inventario.config.AppContextListener;
import es.restaurante.inventario.dto.ProductoProveedorRequestDto;
import es.restaurante.inventario.dto.ProductoRequestDto;
import es.restaurante.inventario.service.ProductoService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/productos/*")
public final class ProductoServlet extends BaseApiServlet {

    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta) throws IOException {
        ejecutar(respuesta, () -> {
            ProductoService servicio = servicio(AppContextListener.PRODUCTO_SERVICE_ATTRIBUTE, ProductoService.class);
            List<String> segmentos = segmentos(solicitud.getPathInfo());
            if (segmentos.isEmpty()) {
                Long categoriaId = idOpcional(solicitud.getParameter("categoriaId"), "categoriaId");
                boolean bajoStock = booleanoOpcional(solicitud.getParameter("bajoStock"), "bajoStock");
                return json(HttpServletResponse.SC_OK, servicio.listar(categoriaId, bajoStock));
            }
            Long productoId = parsearId(segmentos.getFirst(), "id");
            if (segmentos.size() == 1) {
                return json(HttpServletResponse.SC_OK, servicio.obtenerPorId(productoId));
            }
            if (segmentos.size() == 2 && segmentos.get(1).equals("proveedores")) {
                return json(HttpServletResponse.SC_OK, servicio.listarProveedores(productoId));
            }
            throw rutaNoEncontrada();
        });
    }

    @Override
    protected void doPost(HttpServletRequest solicitud, HttpServletResponse respuesta) throws IOException {
        ejecutar(respuesta, () -> {
            List<String> segmentos = segmentos(solicitud.getPathInfo());
            ProductoService servicio = servicio(AppContextListener.PRODUCTO_SERVICE_ATTRIBUTE, ProductoService.class);
            if (segmentos.isEmpty()) {
                ProductoRequestDto dto = leerJson(solicitud, ProductoRequestDto.class);
                return json(HttpServletResponse.SC_CREATED, servicio.crear(dto));
            }
            if (segmentos.size() == 2 && segmentos.get(1).equals("proveedores")) {
                Long productoId = parsearId(segmentos.getFirst(), "id");
                ProductoProveedorRequestDto dto = leerJson(solicitud, ProductoProveedorRequestDto.class);
                return json(HttpServletResponse.SC_CREATED, servicio.agregarProveedor(productoId, dto));
            }
            throw rutaNoEncontrada();
        });
    }

    @Override
    protected void doPut(HttpServletRequest solicitud, HttpServletResponse respuesta) throws IOException {
        ejecutar(respuesta, () -> {
            Long productoId = idDeRuta(solicitud.getPathInfo());
            ProductoRequestDto dto = leerJson(solicitud, ProductoRequestDto.class);
            return json(HttpServletResponse.SC_OK,
                    servicio(AppContextListener.PRODUCTO_SERVICE_ATTRIBUTE, ProductoService.class).actualizar(productoId, dto));
        });
    }

    @Override
    protected void doDelete(HttpServletRequest solicitud, HttpServletResponse respuesta) throws IOException {
        ejecutar(respuesta, () -> {
            List<String> segmentos = segmentos(solicitud.getPathInfo());
            ProductoService servicio = servicio(AppContextListener.PRODUCTO_SERVICE_ATTRIBUTE, ProductoService.class);
            if (segmentos.size() == 1) {
                servicio.eliminar(parsearId(segmentos.getFirst(), "id"));
                return sinContenido();
            }
            if (segmentos.size() == 3 && segmentos.get(1).equals("proveedores")) {
                Long productoId = parsearId(segmentos.get(0), "id");
                Long proveedorId = parsearId(segmentos.get(2), "proveedorId");
                servicio.eliminarProveedor(productoId, proveedorId);
                return sinContenido();
            }
            throw rutaNoEncontrada();
        });
    }

    private Long idOpcional(String valor, String campo) {
        return valor == null || valor.isBlank() ? null : parsearId(valor, campo);
    }

    private boolean booleanoOpcional(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            return false;
        }
        if (valor.equalsIgnoreCase("true")) {
            return true;
        }
        if (valor.equalsIgnoreCase("false")) {
            return false;
        }
        throw new es.restaurante.inventario.exception.ValidacionException("El parámetro debe ser verdadero o falso",
                Map.of(campo, "Usa true o false."));
    }
}