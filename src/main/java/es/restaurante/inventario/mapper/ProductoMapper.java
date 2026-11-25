package es.restaurante.inventario.mapper;

import es.restaurante.inventario.dto.ProductoDto;
import es.restaurante.inventario.dto.ProductoRequestDto;
import es.restaurante.inventario.model.Producto;

import java.math.BigDecimal;

public final class ProductoMapper {

    private ProductoMapper() {
    }

    public static ProductoDto aDto(Producto producto) {
        BigDecimal stockActual = producto.getStockActual();
        BigDecimal stockMinimo = producto.getStockMinimo();
        boolean bajoStock = stockActual != null && stockMinimo != null
                && stockActual.compareTo(stockMinimo) < 0;
        return new ProductoDto(producto.getId(), producto.getNombre(), producto.getCategoriaId(),
                producto.getCategoriaNombre(), producto.getUnidadMedida(), stockActual, stockMinimo,
                producto.getPrecioUnitario(), producto.getFechaActualizacion(), bajoStock);
    }

    public static Producto desdeRequestDto(ProductoRequestDto dto) {
        Producto producto = new Producto();
        producto.setNombre(dto.nombre());
        producto.setCategoriaId(dto.categoriaId());
        producto.setUnidadMedida(dto.unidadMedida());
        producto.setStockActual(BigDecimal.ZERO);
        producto.setStockMinimo(dto.stockMinimo());
        producto.setPrecioUnitario(dto.precioUnitario());
        return producto;
    }
}