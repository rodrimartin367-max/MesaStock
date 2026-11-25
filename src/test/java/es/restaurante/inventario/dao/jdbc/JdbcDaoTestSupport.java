package es.restaurante.inventario.dao.jdbc;

import es.restaurante.inventario.model.Categoria;
import es.restaurante.inventario.model.Producto;
import es.restaurante.inventario.model.Proveedor;
import es.restaurante.inventario.model.UnidadMedida;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.RunScript;
import org.junit.jupiter.api.BeforeEach;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

abstract class JdbcDaoTestSupport {

    protected static final JdbcDataSource DATA_SOURCE = crearDataSource();

    @BeforeEach
    void limpiarTablas() throws SQLException {
        try (Connection conexion = DATA_SOURCE.getConnection();
             Statement sentencia = conexion.createStatement()) {
            sentencia.executeUpdate("DELETE FROM movimiento_stock");
            sentencia.executeUpdate("DELETE FROM producto_proveedor");
            sentencia.executeUpdate("DELETE FROM producto");
            sentencia.executeUpdate("DELETE FROM proveedor");
            sentencia.executeUpdate("DELETE FROM categoria");
        }
    }

    protected long insertarCategoria(String nombre) {
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        return new CategoriaDaoJdbc(DATA_SOURCE).insert(categoria);
    }

    protected long insertarProveedor(String nombre, String cif) {
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre(nombre);
        proveedor.setCif(cif);
        return new ProveedorDaoJdbc(DATA_SOURCE).insert(proveedor);
    }

    protected Producto nuevoProducto(Long categoriaId, String nombre, String stockActual, String stockMinimo) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setCategoriaId(categoriaId);
        producto.setUnidadMedida(UnidadMedida.KG);
        producto.setStockActual(new BigDecimal(stockActual));
        producto.setStockMinimo(new BigDecimal(stockMinimo));
        producto.setPrecioUnitario(new BigDecimal("3.50"));
        return producto;
    }

    protected long insertarProducto(Long categoriaId, String nombre, String stockActual, String stockMinimo) {
        Producto producto = nuevoProducto(categoriaId, nombre, stockActual, stockMinimo);
        return new ProductoDaoJdbc(DATA_SOURCE).insert(producto);
    }

    protected static JdbcDataSource crearDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        try (Connection conexion = dataSource.getConnection();
             InputStream esquema = JdbcDaoTestSupport.class.getResourceAsStream("/schema-h2.sql")) {
            if (esquema == null) {
                throw new IllegalStateException("No se encontró schema-h2.sql");
            }
            RunScript.execute(conexion, new InputStreamReader(esquema, StandardCharsets.UTF_8));
            return dataSource;
        } catch (Exception excepcion) {
            throw new ExceptionInInitializerError(excepcion);
        }
    }
}