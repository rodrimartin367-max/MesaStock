package es.restaurante.inventario.dto;

import es.restaurante.inventario.model.UnidadMedida;

import java.math.BigDecimal;

public record ProductoRequestDto(
        String nombre,
        Long categoriaId,
        UnidadMedida unidadMedida,
        BigDecimal stockMinimo,
        BigDecimal precioUnitario
) {
}