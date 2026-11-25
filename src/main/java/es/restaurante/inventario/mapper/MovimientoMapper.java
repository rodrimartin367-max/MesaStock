package es.restaurante.inventario.mapper;

import es.restaurante.inventario.dto.MovimientoDto;
import es.restaurante.inventario.dto.MovimientoRequestDto;
import es.restaurante.inventario.model.MovimientoStock;

public final class MovimientoMapper {

    private MovimientoMapper() {
    }

    public static MovimientoDto aDto(MovimientoStock movimiento) {
        return new MovimientoDto(movimiento.getId(), movimiento.getProductoId(), movimiento.getProductoNombre(),
                movimiento.getTipo(), movimiento.getCantidad(), movimiento.getFecha(), movimiento.getNota());
    }

    public static MovimientoStock desdeRequestDto(MovimientoRequestDto dto) {
        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setProductoId(dto.productoId());
        movimiento.setTipo(dto.tipo());
        movimiento.setCantidad(dto.cantidad());
        movimiento.setNota(dto.nota());
        return movimiento;
    }
}