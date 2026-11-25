package es.restaurante.inventario.dao;

import es.restaurante.inventario.model.MovimientoStock;

import java.sql.Connection;
import java.util.List;

public interface MovimientoDao {

    List<MovimientoStock> findAll(Long productoId);

    long insert(Connection conexion, MovimientoStock movimiento);
}