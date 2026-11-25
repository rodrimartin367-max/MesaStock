package es.restaurante.inventario.dao.jdbc;

import es.restaurante.inventario.dto.MovimientoRequestDto;
import es.restaurante.inventario.exception.ConflictoException;
import es.restaurante.inventario.model.TipoMovimiento;
import es.restaurante.inventario.service.MovimientoService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MovimientoServiceIntegrationTest extends JdbcDaoTestSupport {

    @Test
    void movimientoFallidoNoCambiaElStockNiElHistorico() {
        long categoriaId = insertarCategoria("Harinas");
        long productoId = insertarProducto(categoriaId, "Harina de trigo", "2.00", "1.00");
        ProductoDaoJdbc productoDao = new ProductoDaoJdbc(DATA_SOURCE);
        MovimientoDaoJdbc movimientoDao = new MovimientoDaoJdbc(DATA_SOURCE);
        MovimientoService servicio = new MovimientoService(DATA_SOURCE, productoDao, movimientoDao);

        assertThrows(ConflictoException.class, () -> servicio.registrar(new MovimientoRequestDto(
                productoId, TipoMovimiento.MERMA, new BigDecimal("2.50"), "Envase roto")));

        assertEquals(new BigDecimal("2.00"), productoDao.findById(productoId).orElseThrow().getStockActual());
        assertTrue(movimientoDao.findAll(productoId).isEmpty());
    }
}