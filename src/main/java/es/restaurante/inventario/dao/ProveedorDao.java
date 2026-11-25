package es.restaurante.inventario.dao;

import es.restaurante.inventario.model.Proveedor;

import java.util.List;
import java.util.Optional;

public interface ProveedorDao {

    List<Proveedor> findAll();

    Optional<Proveedor> findById(Long id);

    long insert(Proveedor proveedor);

    boolean update(Proveedor proveedor);

    boolean delete(Long id);

    boolean existsByCif(String cif);
}