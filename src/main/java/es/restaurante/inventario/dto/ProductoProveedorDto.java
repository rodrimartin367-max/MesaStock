package es.restaurante.inventario.dto;

import java.math.BigDecimal;

public record ProductoProveedorDto(
        Long productoId,
        Long proveedorId,
        String nombreProveedor,
        BigDecimal precioCompra
) {
}