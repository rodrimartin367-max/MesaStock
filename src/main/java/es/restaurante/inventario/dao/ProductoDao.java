package es.restaurante.inventario.dao;

import es.restaurante.inventario.model.Producto;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface ProductoDao {

    List<Producto> findAll(Long categoriaId, boolean soloBajoStock);

    Optional<Producto> findById(Long id);

    long insert(Producto producto);

    boolean update(Producto producto);

    boolean delete(Long id);

    Optional<Producto> findByIdForUpdate(Connection conexion, Long id);

    boolean updateStock(Connection conexion, Long id, BigDecimal nuevoStock);
}