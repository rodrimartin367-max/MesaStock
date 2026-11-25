package es.restaurante.inventario.service;

import es.restaurante.inventario.dao.MovimientoDao;
import es.restaurante.inventario.dao.ProductoDao;
import es.restaurante.inventario.dto.MovimientoDto;
import es.restaurante.inventario.dto.MovimientoRequestDto;
import es.restaurante.inventario.exception.ConflictoException;
import es.restaurante.inventario.exception.DataAccessException;
import es.restaurante.inventario.exception.RecursoNoEncontradoException;
import es.restaurante.inventario.exception.ValidacionException;
import es.restaurante.inventario.mapper.MovimientoMapper;
import es.restaurante.inventario.model.MovimientoStock;
import es.restaurante.inventario.model.Producto;
import es.restaurante.inventario.model.TipoMovimiento;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class MovimientoService {

    private final DataSource dataSource;
    private final ProductoDao productoDao;
    private final MovimientoDao movimientoDao;

    public MovimientoService(DataSource dataSource, ProductoDao productoDao, MovimientoDao movimientoDao) {
        this.dataSource = Objects.requireNonNull(dataSource);
        this.productoDao = Objects.requireNonNull(productoDao);
        this.movimientoDao = Objects.requireNonNull(movimientoDao);
    }

    public List<MovimientoDto> listar(Long productoId) {
        if (productoId != null) {
            buscarProducto(productoId);
        }
        return movimientoDao.findAll(productoId).stream().map(MovimientoMapper::aDto).toList();
    }

    public MovimientoDto registrar(MovimientoRequestDto solicitud) {
        MovimientoStock movimiento = validar(solicitud);
        try (Connection conexion = dataSource.getConnection()) {
            boolean autoCommit = conexion.getAutoCommit();
            RuntimeException fallo = null;
            try {
                conexion.setAutoCommit(false);
                Producto producto = productoDao.findByIdForUpdate(conexion, movimiento.getProductoId())
                        .orElseThrow(() -> new RecursoNoEncontradoException(
                                "No se encontró el producto con id " + movimiento.getProductoId()));
                BigDecimal stockActual = producto.getStockActual();
                BigDecimal nuevoStock = calcularStock(producto, movimiento);
                if (nuevoStock.compareTo(BigDecimal.ZERO) < 0) {
                    throw new ConflictoException("Stock insuficiente: disponible "
                            + stockActual.stripTrailingZeros().toPlainString()
                            + " " + unidad(producto));
                }

                movimiento.setProductoNombre(producto.getNombre());
                movimiento.setId(movimientoDao.insert(conexion, movimiento));
                if (!productoDao.updateStock(conexion, movimiento.getProductoId(), nuevoStock)) {
                    throw new RecursoNoEncontradoException("No se encontró el producto con id "
                            + movimiento.getProductoId());
                }
                conexion.commit();
                return MovimientoMapper.aDto(movimiento);
            } catch (RuntimeException excepcion) {
                fallo = excepcion;
                rollback(conexion, excepcion);
                throw excepcion;
            } catch (SQLException excepcion) {
                fallo = new DataAccessException("No se pudo completar la transacción del movimiento", excepcion);
                rollback(conexion, fallo);
                throw fallo;
            } finally {
                try {
                    conexion.setAutoCommit(autoCommit);
                } catch (SQLException excepcion) {
                    if (fallo != null) {
                        fallo.addSuppressed(excepcion);
                    } else {
                        throw new DataAccessException("No se pudo restaurar el modo de autocommit", excepcion);
                    }
                }
            }
        } catch (SQLException excepcion) {
            throw new DataAccessException("No se pudo obtener una conexión para registrar el movimiento", excepcion);
        }
    }

    private Producto buscarProducto(Long id) {
        return productoDao.findById(id).orElseThrow(() -> new RecursoNoEncontradoException(
                "No se encontró el producto con id " + id));
    }

    private static MovimientoStock validar(MovimientoRequestDto solicitud) {
        Map<String, String> errores = new LinkedHashMap<>();
        if (solicitud == null || solicitud.productoId() == null) {
            errores.put("productoId", "Es obligatorio.");
        }
        if (solicitud == null || solicitud.tipo() == null) {
            errores.put("tipo", "Es obligatorio y debe ser ENTRADA, SALIDA o MERMA.");
        }
        BigDecimal cantidad = solicitud == null ? null : solicitud.cantidad();
        if (cantidad == null) {
            errores.put("cantidad", "Es obligatorio.");
        } else if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            errores.put("cantidad", "Debe ser mayor que cero.");
        }
        String nota = solicitud == null ? null
                : ValidacionesServicio.texto(errores, "nota", solicitud.nota(), 255, false);
        ValidacionesServicio.lanzarSiHayErrores(errores);

        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setProductoId(solicitud.productoId());
        movimiento.setTipo(solicitud.tipo());
        movimiento.setCantidad(cantidad);
        movimiento.setNota(nota);
        return movimiento;
    }

    private static BigDecimal calcularStock(Producto producto, MovimientoStock movimiento) {
        BigDecimal stock = producto.getStockActual();
        return movimiento.getTipo() == TipoMovimiento.ENTRADA
                ? stock.add(movimiento.getCantidad()) : stock.subtract(movimiento.getCantidad());
    }

    private static String unidad(Producto producto) {
        if (producto.getUnidadMedida() == null) {
            return "unidades";
        }
        return switch (producto.getUnidadMedida()) {
            case KG -> "kg";
            case L -> "l";
            case UD -> "ud";
        };
    }

    private static void rollback(Connection conexion, RuntimeException fallo) {
        try {
            conexion.rollback();
        } catch (SQLException excepcion) {
            fallo.addSuppressed(excepcion);
        }
    }
}