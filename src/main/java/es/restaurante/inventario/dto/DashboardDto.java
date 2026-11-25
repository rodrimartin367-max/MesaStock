package es.restaurante.inventario.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardDto(
        long totalProductos,
        long totalCategorias,
        long totalProveedores,
        BigDecimal valorInventario,
        List<ProductoDto> productosBajoStock
) {
    public DashboardDto {
        productosBajoStock = List.copyOf(productosBajoStock);
    }
}