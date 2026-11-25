package es.restaurante.inventario.service;

import es.restaurante.inventario.dao.CategoriaDao;
import es.restaurante.inventario.dao.ProductoDao;
import es.restaurante.inventario.dao.ProveedorDao;
import es.restaurante.inventario.dto.DashboardDto;
import es.restaurante.inventario.dto.ProductoDto;
import es.restaurante.inventario.mapper.ProductoMapper;
import es.restaurante.inventario.model.Producto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public final class DashboardService {

    private final ProductoDao productoDao;
    private final CategoriaDao categoriaDao;
    private final ProveedorDao proveedorDao;

    public DashboardService(ProductoDao productoDao, CategoriaDao categoriaDao, ProveedorDao proveedorDao) {
        this.productoDao = Objects.requireNonNull(productoDao);
        this.categoriaDao = Objects.requireNonNull(categoriaDao);
        this.proveedorDao = Objects.requireNonNull(proveedorDao);
    }

    public DashboardDto obtener() {
        List<Producto> productos = productoDao.findAll(null, false);
        BigDecimal valorInventario = productos.stream()
                .map(producto -> producto.getStockActual().multiply(producto.getPrecioUnitario()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<ProductoDto> productosBajoStock = productos.stream()
                .filter(producto -> producto.getStockActual().compareTo(producto.getStockMinimo()) < 0)
                .map(ProductoMapper::aDto)
                .toList();
        return new DashboardDto(productos.size(), categoriaDao.findAll().size(), proveedorDao.findAll().size(),
                valorInventario, productosBajoStock);
    }
}