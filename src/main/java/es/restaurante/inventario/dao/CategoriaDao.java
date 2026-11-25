package es.restaurante.inventario.dao;

import es.restaurante.inventario.model.Categoria;

import java.util.List;
import java.util.Optional;

public interface CategoriaDao {

    List<Categoria> findAll();

    Optional<Categoria> findById(Long id);

    long insert(Categoria categoria);

    boolean update(Categoria categoria);

    boolean delete(Long id);

    boolean existsByNombre(String nombre);

    long countProductos(Long categoriaId);
}