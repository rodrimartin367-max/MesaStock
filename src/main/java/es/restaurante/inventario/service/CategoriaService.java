package es.restaurante.inventario.service;

import es.restaurante.inventario.dao.CategoriaDao;
import es.restaurante.inventario.dto.CategoriaDto;
import es.restaurante.inventario.dto.CategoriaRequestDto;
import es.restaurante.inventario.exception.ConflictoException;
import es.restaurante.inventario.exception.RecursoNoEncontradoException;
import es.restaurante.inventario.mapper.CategoriaMapper;
import es.restaurante.inventario.model.Categoria;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CategoriaService {

    private final CategoriaDao categoriaDao;

    public CategoriaService(CategoriaDao categoriaDao) {
        this.categoriaDao = Objects.requireNonNull(categoriaDao);
    }

    public List<CategoriaDto> listar() {
        return categoriaDao.findAll().stream().map(CategoriaMapper::aDto).toList();
    }

    public CategoriaDto obtenerPorId(Long id) {
        return CategoriaMapper.aDto(buscar(id));
    }

    public CategoriaDto crear(CategoriaRequestDto solicitud) {
        Categoria categoria = validar(solicitud);
        categoria.setId(categoriaDao.insert(categoria));
        return CategoriaMapper.aDto(categoria);
    }

    public CategoriaDto actualizar(Long id, CategoriaRequestDto solicitud) {
        buscar(id);
        Categoria categoria = validar(solicitud);
        categoria.setId(id);
        if (!categoriaDao.update(categoria)) {
            throw noEncontrada(id);
        }
        return CategoriaMapper.aDto(categoria);
    }

    public void eliminar(Long id) {
        buscar(id);
        long productos = categoriaDao.countProductos(id);
        if (productos > 0) {
            throw new ConflictoException("La categoría tiene " + productos + " productos");
        }
        if (!categoriaDao.delete(id)) {
            throw noEncontrada(id);
        }
    }

    private Categoria buscar(Long id) {
        if (id == null) {
            throw noEncontrada(null);
        }
        return categoriaDao.findById(id).orElseThrow(() -> noEncontrada(id));
    }

    private static Categoria validar(CategoriaRequestDto solicitud) {
        Map<String, String> errores = new LinkedHashMap<>();
        String nombre = solicitud == null ? null
                : ValidacionesServicio.texto(errores, "nombre", solicitud.nombre(), 60, true);
        String descripcion = solicitud == null ? null
                : ValidacionesServicio.texto(errores, "descripcion", solicitud.descripcion(), 255, false);
        if (solicitud == null) {
            errores.put("nombre", "Es obligatorio.");
        }
        ValidacionesServicio.lanzarSiHayErrores(errores);

        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setDescripcion(descripcion);
        return categoria;
    }

    private static RecursoNoEncontradoException noEncontrada(Long id) {
        return new RecursoNoEncontradoException("No se encontró la categoría" + (id == null ? "" : " con id " + id));
    }
}