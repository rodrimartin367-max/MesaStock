package es.restaurante.inventario.mapper;

import es.restaurante.inventario.dto.ProductoProveedorDto;
import es.restaurante.inventario.dto.ProductoProveedorRequestDto;
import es.restaurante.inventario.model.ProductoProveedor;

public final class ProductoProveedorMapper {

    private ProductoProveedorMapper() {
    }

    public static ProductoProveedorDto aDto(ProductoProveedor relacion) {
        return new ProductoProveedorDto(relacion.getProductoId(), relacion.getProveedorId(),
                relacion.getNombreProveedor(), relacion.getPrecioCompra());
    }

    public static ProductoProveedor desdeRequestDto(ProductoProveedorRequestDto dto) {
        ProductoProveedor relacion = new ProductoProveedor();
        relacion.setProveedorId(dto.proveedorId());
        relacion.setPrecioCompra(dto.precioCompra());
        return relacion;
    }
}