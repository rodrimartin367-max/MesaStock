package es.restaurante.inventario.mapper;

import es.restaurante.inventario.dto.CategoriaDto;
import es.restaurante.inventario.dto.CategoriaRequestDto;
import es.restaurante.inventario.model.Categoria;

public final class CategoriaMapper {

    private CategoriaMapper() {
    }

    public static CategoriaDto aDto(Categoria categoria) {
        return new CategoriaDto(categoria.getId(), categoria.getNombre(), categoria.getDescripcion());
    }

    public static Categoria desdeRequestDto(CategoriaRequestDto dto) {
        Categoria categoria = new Categoria();
        categoria.setNombre(dto.nombre());
        categoria.setDescripcion(dto.descripcion());
        return categoria;
    }
}