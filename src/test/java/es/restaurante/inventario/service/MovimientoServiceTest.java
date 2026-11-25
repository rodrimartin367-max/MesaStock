package es.restaurante.inventario.service;

import es.restaurante.inventario.dao.MovimientoDao;
import es.restaurante.inventario.dao.ProductoDao;
import es.restaurante.inventario.dto.MovimientoRequestDto;
import es.restaurante.inventario.exception.ConflictoException;
import es.restaurante.inventario.exception.ValidacionException;
import es.restaurante.inventario.model.Producto;
import es.restaurante.inventario.model.TipoMovimiento;
import es.restaurante.inventario.model.UnidadMedida;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimientoServiceTest {

    @Mock
    private DataSource dataSource;
    @Mock
    private ProductoDao productoDao;
    @Mock
    private MovimientoDao movimientoDao;
    @Mock
    private Connection conexion;

    private MovimientoService servicio;

    @BeforeEach
    void setUp() {
        servicio = new MovimientoService(dataSource, productoDao, movimientoDao);
    }

    @Test
    void salidaMayorQueStockHaceRollback() throws SQLException {
        prepararTransaccion(new BigDecimal("2.00"));

        ConflictoException excepcion = assertThrows(ConflictoException.class,
                () -> servicio.registrar(new MovimientoRequestDto(1L, TipoMovimiento.SALIDA,
                        new BigDecimal("3.00"), null)));

        assertEquals("Stock insuficiente: disponible 2 kg", excepcion.getMessage());
        verify(conexion).rollback();
        verify(conexion, never()).commit();
        verify(movimientoDao, never()).insert(eq(conexion), any());
        verify(productoDao, never()).updateStock(eq(conexion), eq(1L), any());
        verify(conexion).setAutoCommit(true);
    }

    @Test
    void entradaActualizaStockYConfirmaTransaccion() throws SQLException {
        prepararTransaccion(new BigDecimal("2.00"));
        when(movimientoDao.insert(eq(conexion), any())).thenReturn(15L);
        when(productoDao.updateStock(conexion, 1L, new BigDecimal("5.00"))).thenReturn(true);

        var dto = servicio.registrar(new MovimientoRequestDto(1L, TipoMovimiento.ENTRADA,
                new BigDecimal("3.00"), " Albarán "));

        assertEquals(15L, dto.id());
        assertEquals(1L, dto.productoId());
        verify(movimientoDao).insert(eq(conexion), any());
        verify(productoDao).updateStock(conexion, 1L, new BigDecimal("5.00"));
        verify(conexion).commit();
        verify(conexion, never()).rollback();
        verify(conexion).setAutoCommit(true);
    }

    @Test
    void informaCamposInvalidosAntesDeAbrirLaConexion() throws SQLException {
        ValidacionException excepcion = assertThrows(ValidacionException.class,
                () -> servicio.registrar(new MovimientoRequestDto(null, null, BigDecimal.ZERO, "x".repeat(256))));

        assertEquals(4, excepcion.getErrores().size());
        verify(dataSource, never()).getConnection();
    }

    private void prepararTransaccion(BigDecimal stock) throws SQLException {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Harina");
        producto.setUnidadMedida(UnidadMedida.KG);
        producto.setStockActual(stock);
        when(dataSource.getConnection()).thenReturn(conexion);
        when(conexion.getAutoCommit()).thenReturn(true);
        when(productoDao.findByIdForUpdate(conexion, 1L)).thenReturn(Optional.of(producto));
    }
}