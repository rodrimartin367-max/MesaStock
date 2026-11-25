package es.restaurante.inventario.dao.jdbc;

import es.restaurante.inventario.dao.ProductoProveedorDao;
import es.restaurante.inventario.model.ProductoProveedor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProductoProveedorDaoJdbc implements ProductoProveedorDao {

    private final DataSource dataSource;

    public ProductoProveedorDaoJdbc(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    @Override
    public List<ProductoProveedor> findByProducto(Long productoId) {
        String sql = "SELECT pp.producto_id, pp.proveedor_id, p.nombre AS nombre_proveedor, pp.precio_compra "
                + "FROM producto_proveedor pp JOIN proveedor p ON pp.proveedor_id = p.id "
                + "WHERE pp.producto_id = ? ORDER BY p.nombre";
        List<ProductoProveedor> relaciones = new ArrayList<>();
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, productoId);
            try (ResultSet resultados = sentencia.executeQuery()) {
                while (resultados.next()) {
                    relaciones.add(mapear(resultados));
                }
            }
            return relaciones;
        } catch (SQLException excepcion) {
            throw JdbcExceptionMapper.convertir(excepcion, "No se pudo consultar la relación con el proveedor",
                    "Error al listar los proveedores del producto");
        }
    }

    @Override
    public void insert(ProductoProveedor relacion) {
        String sql = "INSERT INTO producto_proveedor (producto_id, proveedor_id, precio_compra) VALUES (?, ?, ?)";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, relacion.getProductoId());
            sentencia.setLong(2, relacion.getProveedorId());
            sentencia.setBigDecimal(3, relacion.getPrecioCompra());
            sentencia.executeUpdate();
        } catch (SQLException excepcion) {
            throw JdbcExceptionMapper.convertir(excepcion,
                    "La relación producto-proveedor ya existe o alguno de los recursos no existe",
                    "Error al asociar el proveedor al producto");
        }
    }

    @Override
    public boolean delete(Long productoId, Long proveedorId) {
        String sql = "DELETE FROM producto_proveedor WHERE producto_id = ? AND proveedor_id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, productoId);
            sentencia.setLong(2, proveedorId);
            return sentencia.executeUpdate() > 0;
        } catch (SQLException excepcion) {
            throw JdbcExceptionMapper.convertir(excepcion, "No se pudo borrar la relación producto-proveedor",
                    "Error al borrar el proveedor del producto");
        }
    }

    private static ProductoProveedor mapear(ResultSet resultado) throws SQLException {
        ProductoProveedor relacion = new ProductoProveedor();
        relacion.setProductoId(resultado.getLong("producto_id"));
        relacion.setProveedorId(resultado.getLong("proveedor_id"));
        relacion.setNombreProveedor(resultado.getString("nombre_proveedor"));
        relacion.setPrecioCompra(resultado.getBigDecimal("precio_compra"));
        return relacion;
    }
}