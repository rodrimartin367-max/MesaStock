package es.restaurante.inventario.dao;

import es.restaurante.inventario.model.ProductoProveedor;

import java.util.List;

public interface ProductoProveedorDao {

    List<ProductoProveedor> findByProducto(Long productoId);

    void insert(ProductoProveedor relacion);

    boolean delete(Long productoId, Long proveedorId);
}