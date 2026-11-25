package es.restaurante.inventario.dao.jdbc;

import es.restaurante.inventario.dao.CategoriaDao;
import es.restaurante.inventario.model.Categoria;

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

public class CategoriaDaoJdbc implements CategoriaDao {

    private final DataSource dataSource;

    public CategoriaDaoJdbc(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    @Override
    public List<Categoria> findAll() {
        String sql = "SELECT id, nombre, descripcion FROM categoria ORDER BY nombre";
        List<Categoria> categorias = new ArrayList<>();
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultados = sentencia.executeQuery()) {
            while (resultados.next()) {
                categorias.add(mapear(resultados));
            }
            return categorias;
        } catch (SQLException excepcion) {
            throw JdbcExceptionMapper.convertir(excepcion, "No se pudo consultar la categoría", "Error al listar las categorías");
        }
    }

    @Override
    public Optional<Categoria> findById(Long id) {
        String sql = "SELECT id, nombre, descripcion FROM categoria WHERE id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            try (ResultSet resultados = sentencia.executeQuery()) {
                return resultados.next() ? Optional.of(mapear(resultados)) : Optional.empty();
            }
        } catch (SQLException excepcion) {
            throw JdbcExceptionMapper.convertir(excepcion, "No se pudo consultar la categoría", "Error al buscar la categoría");
        }
    }

    @Override
    public long insert(Categoria categoria) {
        String sql = "INSERT INTO categoria (nombre, descripcion) VALUES (?, ?)";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, categoria.getNombre());
            sentencia.setString(2, categoria.getDescripcion());
            sentencia.executeUpdate();
            long id = JdbcDaoUtils.obtenerIdGenerado(sentencia, "la categoría");
            categoria.setId(id);
            return id;
        } catch (SQLException excepcion) {
            throw JdbcExceptionMapper.convertir(excepcion, "Ya existe una categoría con ese nombre", "Error al crear la categoría");
        }
    }

    @Override
    public boolean update(Categoria categoria) {
        String sql = "UPDATE categoria SET nombre = ?, descripcion = ? WHERE id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, categoria.getNombre());
            sentencia.setString(2, categoria.getDescripcion());
            sentencia.setLong(3, categoria.getId());
            return sentencia.executeUpdate() > 0;
        } catch (SQLException excepcion) {
            throw JdbcExceptionMapper.convertir(excepcion, "Ya existe una categoría con ese nombre", "Error al actualizar la categoría");
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM categoria WHERE id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            return sentencia.executeUpdate() > 0;
        } catch (SQLException excepcion) {
            throw JdbcExceptionMapper.convertir(excepcion,
                    "No se puede borrar la categoría porque tiene productos asociados", "Error al borrar la categoría");
        }
    }

    @Override
    public boolean existsByNombre(String nombre) {
        String sql = "SELECT 1 FROM categoria WHERE nombre = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, nombre);
            try (ResultSet resultados = sentencia.executeQuery()) {
                return resultados.next();
            }
        } catch (SQLException excepcion) {
            throw JdbcExceptionMapper.convertir(excepcion, "No se pudo comprobar el nombre de la categoría",
                    "Error al comprobar la categoría");
        }
    }

    @Override
    public long countProductos(Long categoriaId) {
        String sql = "SELECT COUNT(*) FROM producto WHERE categoria_id = ?";
        try (Connection conexion = dataSource.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, categoriaId);
            try (ResultSet resultados = sentencia.executeQuery()) {
                resultados.next();
                return resultados.getLong(1);
            }
        } catch (SQLException excepcion) {
            throw JdbcExceptionMapper.convertir(excepcion, "No se pudo contar los productos de la categoría",
                    "Error al contar los productos de la categoría");
        }
    }

    private static Categoria mapear(ResultSet resultado) throws SQLException {
        Categoria categoria = new Categoria();
        categoria.setId(resultado.getLong("id"));
        categoria.setNombre(resultado.getString("nombre"));
        categoria.setDescripcion(resultado.getString("descripcion"));
        return categoria;
    }
}