package es.restaurante.inventario.dao.jdbc;

import es.restaurante.inventario.dao.ProductoDao;
import es.restaurante.inventario.model.Producto;
import es.restaurante.inventario.model.UnidadMedida;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ProductoDaoJdbc implements ProductoDao {

    private static final String COLUMNAS_PRODUCTO = "SELECT p.id, p.nombre, p.categoria_id, c.nombre AS categoria_nombre, "
            + "p.unidad_medida, p.stock_actual, p.stock_minimo, p.precio_unitario, p.fecha_actualizacion "
            + "FROM producto p JOIN categoria c ON p.categoria_id = c.id";

    private final DataSource dataSource;

    public ProductoDaoJdbc(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    @Override
    public List<Producto> findAll(Long categoriaId, boolean soloBajoStock) {
        StringBuilder sql = new StringBuilder(COLUMNAS_PRODUCTO);
        boolean hayCondicion = false;
        if (categoriaId != null) {
            sql.append(" WHERE p.categoria_id = ?");
            hayCondicion = true;
        }
        if (soloBajoStock) {
            sql.append(hayCondicion ? " AND" : " WHERE").append(" p.stock_actual < p.stock_minimo");
        }
        sql.append(" ORDER BY p.nombre");

        List<Producto> productos = new ArrayList<>();
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql.toString())) {
            if (categoriaId != null) {
                sentencia.setLong(1, categoriaId);
            }
            try (ResultSet resultados = sentencia.executeQuery()) {
                while (resultados.next()) {
                    productos.add(mapear(resultados));
                }
            }
            return productos;
        } catch (SQLException excepcion) {
            throw JdbcExceptionMapper.convertir(excepcion, "No se pudo consultar el producto", "Error al listar los productos");
        }
    }

    @Override
    public Optional<Producto> findById(Long id) {
        String sql = COLUMNAS_PRODUCTO + " WHERE p.id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            try (ResultSet resultados = sentencia.executeQuery()) {
                return resultados.next() ? Optional.of(mapear(resultados)) : Optional.empty();
            }
        } catch (SQLException excepcion) {
            throw JdbcExceptionMapper.convertir(excepcion, "No se pudo consultar el producto", "Error al buscar el producto");
        }
    }

    @Override
    public long insert(Producto producto) {
        String sql = "INSERT INTO producto (nombre, categoria_id, unidad_medida, stock_actual, stock_minimo, "
                + "precio_unitario, fecha_actualizacion) VALUES (?, ?, ?, ?, ?, ?, ?)";
        LocalDateTime fechaActualizacion = LocalDateTime.now();
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, producto.getNombre());
            sentencia.setLong(2, producto.getCategoriaId());
            sentencia.setString(3, producto.getUnidadMedida().name());
            sentencia.setBigDecimal(4, producto.getStockActual());
            sentencia.setBigDecimal(5, producto.getStockMinimo());
            sentencia.setBigDecimal(6, producto.getPrecioUnitario());
            sentencia.setTimestamp(7, Timestamp.valueOf(fechaActualizacion));
            sentencia.executeUpdate();
            long id = JdbcDaoUtils.obtenerIdGenerado(sentencia, "el producto");
            producto.setId(id);
            producto.setFechaActualizacion(fechaActualizacion);
            return id;
        } catch (SQLException excepcion) {
            throw JdbcExceptionMapper.convertir(excepcion,
                    "El producto ya existe o la categoría indicada no existe", "Error al crear el producto");
        }
    }

    @Override
    public boolean update(Producto producto) {
        String sql = "UPDATE producto SET nombre = ?, categoria_id = ?, unidad_medida = ?, stock_minimo = ?, "
                + "precio_unitario = ?, fecha_actualizacion = ? WHERE id = ?";
        LocalDateTime fechaActualizacion = LocalDateTime.now();
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, producto.getNombre());
            sentencia.setLong(2, producto.getCategoriaId());
            sentencia.setString(3, producto.getUnidadMedida().name());
            sentencia.setBigDecimal(4, producto.getStockMinimo());
            sentencia.setBigDecimal(5, producto.getPrecioUnitario());
            sentencia.setTimestamp(6, Timestamp.valueOf(fechaActualizacion));
            sentencia.setLong(7, producto.getId());
            boolean actualizado = sentencia.executeUpdate() > 0;
            if (actualizado) {
                producto.setFechaActualizacion(fechaActualizacion);
            }
            return actualizado;
        } catch (SQLException excepcion) {
            throw JdbcExceptionMapper.convertir(excepcion,
                    "El producto ya existe o la categoría indicada no existe", "Error al actualizar el producto");
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM producto WHERE id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            return sentencia.executeUpdate() > 0;
        } catch (SQLException excepcion) {
            throw JdbcExceptionMapper.convertir(excepcion, "No se puede borrar el producto por sus dependencias",
                    "Error al borrar el producto");
        }
    }

    @Override
    public Optional<Producto> findByIdForUpdate(Connection conexion, Long id) {
        String sql = COLUMNAS_PRODUCTO + " WHERE p.id = ? FOR UPDATE";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            try (ResultSet resultados = sentencia.executeQuery()) {
                return resultados.next() ? Optional.of(mapear(resultados)) : Optional.empty();
            }
        } catch (SQLException excepcion) {
            throw JdbcExceptionMapper.convertir(excepcion, "No se pudo bloquear el producto", "Error al bloquear el producto");
        }
    }

    @Override
    public boolean updateStock(Connection conexion, Long id, BigDecimal nuevoStock) {
        String sql = "UPDATE producto SET stock_actual = ?, fecha_actualizacion = ? WHERE id = ?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setBigDecimal(1, nuevoStock);
            sentencia.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            sentencia.setLong(3, id);
            return sentencia.executeUpdate() > 0;
        } catch (SQLException excepcion) {
            throw JdbcExceptionMapper.convertir(excepcion, "El stock indicado no es válido", "Error al actualizar el stock");
        }
    }

    private static Producto mapear(ResultSet resultado) throws SQLException {
        Producto producto = new Producto();
        producto.setId(resultado.getLong("id"));
        producto.setNombre(resultado.getString("nombre"));
        producto.setCategoriaId(resultado.getLong("categoria_id"));
        producto.setCategoriaNombre(resultado.getString("categoria_nombre"));
        producto.setUnidadMedida(UnidadMedida.valueOf(resultado.getString("unidad_medida")));
        producto.setStockActual(resultado.getBigDecimal("stock_actual"));
        producto.setStockMinimo(resultado.getBigDecimal("stock_minimo"));
        producto.setPrecioUnitario(resultado.getBigDecimal("precio_unitario"));
        Timestamp fechaActualizacion = resultado.getTimestamp("fecha_actualizacion");
        producto.setFechaActualizacion(fechaActualizacion == null ? null : fechaActualizacion.toLocalDateTime());
        return producto;
    }
}