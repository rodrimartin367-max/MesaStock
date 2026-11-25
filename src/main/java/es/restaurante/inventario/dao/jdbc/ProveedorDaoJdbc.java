package es.restaurante.inventario.dao.jdbc;

import es.restaurante.inventario.dao.ProveedorDao;
import es.restaurante.inventario.model.Proveedor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ProveedorDaoJdbc implements ProveedorDao {

    private final DataSource dataSource;

    public ProveedorDaoJdbc(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    @Override
    public List<Proveedor> findAll() {
        String sql = "SELECT id, nombre, cif, telefono, email FROM proveedor ORDER BY nombre";
        List<Proveedor> proveedores = new ArrayList<>();
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultados = sentencia.executeQuery()) {
            while (resultados.next()) {
                proveedores.add(mapear(resultados));
            }
            return proveedores;
        } catch (SQLException excepcion) {
            throw JdbcExceptionMapper.convertir(excepcion, "No se pudo consultar el proveedor", "Error al listar los proveedores");
        }
    }

    @Override
    public Optional<Proveedor> findById(Long id) {
        String sql = "SELECT id, nombre, cif, telefono, email FROM proveedor WHERE id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            try (ResultSet resultados = sentencia.executeQuery()) {
                return resultados.next() ? Optional.of(mapear(resultados)) : Optional.empty();
            }
        } catch (SQLException excepcion) {
            throw JdbcExceptionMapper.convertir(excepcion, "No se pudo consultar el proveedor", "Error al buscar el proveedor");
        }
    }

    @Override
    public long insert(Proveedor proveedor) {
        String sql = "INSERT INTO proveedor (nombre, cif, telefono, email) VALUES (?, ?, ?, ?)";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            establecerCampos(sentencia, proveedor);
            sentencia.executeUpdate();
            long id = JdbcDaoUtils.obtenerIdGenerado(sentencia, "el proveedor");
            proveedor.setId(id);
            return id;
        } catch (SQLException excepcion) {
            throw JdbcExceptionMapper.convertir(excepcion,
                    "El CIF o el email del proveedor ya está registrado", "Error al crear el proveedor");
        }
    }

    @Override
    public boolean update(Proveedor proveedor) {
        String sql = "UPDATE proveedor SET nombre = ?, cif = ?, telefono = ?, email = ? WHERE id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            establecerCampos(sentencia, proveedor);
            sentencia.setLong(5, proveedor.getId());
            return sentencia.executeUpdate() > 0;
        } catch (SQLException excepcion) {
            throw JdbcExceptionMapper.convertir(excepcion,
                    "El CIF o el email del proveedor ya está registrado", "Error al actualizar el proveedor");
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM proveedor WHERE id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            return sentencia.executeUpdate() > 0;
        } catch (SQLException excepcion) {
            throw JdbcExceptionMapper.convertir(excepcion, "No se puede borrar el proveedor por sus dependencias",
                    "Error al borrar el proveedor");
        }
    }

    @Override
    public boolean existsByCif(String cif) {
        String sql = "SELECT 1 FROM proveedor WHERE cif = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, cif);
            try (ResultSet resultados = sentencia.executeQuery()) {
                return resultados.next();
            }
        } catch (SQLException excepcion) {
            throw JdbcExceptionMapper.convertir(excepcion, "No se pudo comprobar el CIF del proveedor",
                    "Error al comprobar el proveedor");
        }
    }

    private static void establecerCampos(PreparedStatement sentencia, Proveedor proveedor) throws SQLException {
        sentencia.setString(1, proveedor.getNombre());
        sentencia.setString(2, proveedor.getCif());
        sentencia.setString(3, proveedor.getTelefono());
        sentencia.setString(4, proveedor.getEmail());
    }

    private static Proveedor mapear(ResultSet resultado) throws SQLException {
        Proveedor proveedor = new Proveedor();
        proveedor.setId(resultado.getLong("id"));
        proveedor.setNombre(resultado.getString("nombre"));
        proveedor.setCif(resultado.getString("cif"));
        proveedor.setTelefono(resultado.getString("telefono"));
        proveedor.setEmail(resultado.getString("email"));
        return proveedor;
    }
}