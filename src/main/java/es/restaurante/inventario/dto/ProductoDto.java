package es.restaurante.inventario.dto;

import es.restaurante.inventario.model.UnidadMedida;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductoDto(
        Long id,
        String nombre,
        Long categoriaId,
        String categoriaNombre,
        UnidadMedida unidadMedida,
        BigDecimal stockActual,
        BigDecimal stockMinimo,
        BigDecimal precioUnitario,
        LocalDateTime fechaActualizacion,
        boolean bajoStock
) {
}