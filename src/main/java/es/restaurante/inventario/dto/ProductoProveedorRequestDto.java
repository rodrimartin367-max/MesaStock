package es.restaurante.inventario.dto;

import java.math.BigDecimal;

public record ProductoProveedorRequestDto(Long proveedorId, BigDecimal precioCompra) {
}