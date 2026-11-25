package es.restaurante.inventario.dto;

import es.restaurante.inventario.model.TipoMovimiento;

import java.math.BigDecimal;

public record MovimientoRequestDto(Long productoId, TipoMovimiento tipo, BigDecimal cantidad, String nota) {
}