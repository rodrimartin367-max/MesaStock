package es.restaurante.inventario.dto;

import es.restaurante.inventario.model.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoDto(
        Long id,
        Long productoId,
        String productoNombre,
        TipoMovimiento tipo,
        BigDecimal cantidad,
        LocalDateTime fecha,
        String nota
) {
}