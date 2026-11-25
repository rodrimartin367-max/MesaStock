package es.restaurante.inventario.mapper;

import es.restaurante.inventario.dto.ProductoDto;
import es.restaurante.inventario.model.Producto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductoMapperTest {

    @Test
    void marcaBajoStockSoloCuandoElStockActualEsInferiorAlMinimo() {
        Producto producto = new Producto();
        producto.setStockActual(new BigDecimal("2.00"));
        producto.setStockMinimo(new BigDecimal("3.00"));

        ProductoDto dto = ProductoMapper.aDto(producto);

        assertTrue(dto.bajoStock());
    }
}