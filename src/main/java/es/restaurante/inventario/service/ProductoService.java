package es.restaurante.inventario.service;

import es.restaurante.inventario.dao.CategoriaDao;
import es.restaurante.inventario.dao.ProductoDao;
import es.restaurante.inventario.dao.ProductoProveedorDao;
import es.restaurante.inventario.dao.ProveedorDao;
import es.restaurante.inventario.dto.ProductoDto;
import es.restaurante.inventario.dto.ProductoProveedorDto;
import es.restaurante.inventario.dto.ProductoProveedorRequestDto;
import es.restaurante.inventario.dto.ProductoRequestDto;
import es.restaurante.inventario.exception.RecursoNoEncontradoException;
import es.restaurante.inventario.mapper.ProductoMapper;
import es.restaurante.inventario.mapper.ProductoProveedorMapper;
import es.restaurante.inventario.model.Producto;
import es.restaurante.inventario.model.ProductoProveedor;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ProductoService {

    private final ProductoDao productoDao;
    private final CategoriaDao categoriaDao;
    private final ProveedorDao proveedorDao;
    private final ProductoProveedorDao productoProveedorDao;

    public ProductoService(ProductoDao productoDao, CategoriaDao categoriaDao, ProveedorDao proveedorDao,
                           ProductoProveedorDao productoProveedorDao) {
        this.productoDao = Objects.requireNonNull(productoDao);
        this.categoriaDao = Objects.requireNonNull(categoriaDao);
        this.proveedorDao = Objects.requireNonNull(proveedorDao);
        this.productoProveedorDao = Objects.requireNonNull(productoProveedorDao);
    }

    public List<ProductoDto> listar(Long categoriaId, boolean soloBajoStock) {
        if (categoriaId != null) {
            buscarCategoria(categoriaId);
        }
        return productoDao.findAll(categoriaId, soloBajoStock).stream().map(ProductoMapper::aDto).toList();
    }

    public ProductoDto obtenerPorId(Long id) {
        return ProductoMapper.aDto(buscar(id));
    }

    public ProductoDto crear(ProductoRequestDto solicitud) {
        Producto producto = validar(solicitud);
        buscarCategoria(producto.getCategoriaId());
        producto.setStockActual(BigDecimal.ZERO);
        producto.setId(productoDao.insert(producto));
        return ProductoMapper.aDto(producto);
    }

    public ProductoDto actualizar(Long id, ProductoRequestDto solicitud) {
        Producto existente = buscar(id);
        Producto producto = validar(solicitud);
        buscarCategoria(producto.getCategoriaId());
        producto.setId(id);
        producto.setStockActual(existente.getStockActual());
        producto.setFechaActualizacion(existente.getFechaActualizacion());
        if (!productoDao.update(producto)) {
            throw noEncontrado(id);
        }
        return ProductoMapper.aDto(producto);
    }

    public void eliminar(Long id) {
        buscar(id);
        if (!productoDao.delete(id)) {
            throw noEncontrado(id);
        }
    }

    public List<ProductoProveedorDto> listarProveedores(Long productoId) {
        buscar(productoId);
        return productoProveedorDao.findByProducto(productoId).stream()
                .map(ProductoProveedorMapper::aDto).toList();
    }

    public ProductoProveedorDto agregarProveedor(Long productoId, ProductoProveedorRequestDto solicitud) {
        buscar(productoId);
        ProductoProveedor relacion = validarRelacion(solicitud);
        var proveedor = proveedorDao.findById(relacion.getProveedorId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el proveedor con id " + relacion.getProveedorId()));
        relacion.setProductoId(productoId);
        relacion.setNombreProveedor(proveedor.getNombre());
        productoProveedorDao.insert(relacion);
        return ProductoProveedorMapper.aDto(relacion);
    }

    public void eliminarProveedor(Long productoId, Long proveedorId) {
        buscar(productoId);
        proveedorDao.findById(proveedorId).orElseThrow(() -> new RecursoNoEncontradoException(
                "No se encontró el proveedor" + (proveedorId == null ? "" : " con id " + proveedorId)));
        if (!productoProveedorDao.delete(productoId, proveedorId)) {
            throw new RecursoNoEncontradoException("No existe la relación entre el producto y el proveedor");
        }
    }

    private Producto buscar(Long id) {
        if (id == null) {
            throw noEncontrado(null);
        }
        return productoDao.findById(id).orElseThrow(() -> noEncontrado(id));
    }

    private void buscarCategoria(Long id) {
        if (id == null || categoriaDao.findById(id).isEmpty()) {
            throw new RecursoNoEncontradoException("No se encontró la categoría" + (id == null ? "" : " con id " + id));
        }
    }

    private static Producto validar(ProductoRequestDto solicitud) {
        Map<String, String> errores = new LinkedHashMap<>();
        String nombre = solicitud == null ? null
                : ValidacionesServicio.texto(errores, "nombre", solicitud.nombre(), 100, true);
        if (solicitud == null || solicitud.categoriaId() == null) {
            errores.put("categoriaId", "Es obligatorio.");
        }
        if (solicitud == null || solicitud.unidadMedida() == null) {
            errores.put("unidadMedida", "Es obligatorio y debe ser KG, L o UD.");
        }
        BigDecimal stockMinimo = solicitud == null ? null : solicitud.stockMinimo();
        validarImporte(errores, "stockMinimo", stockMinimo);
        BigDecimal precioUnitario = solicitud == null ? null : solicitud.precioUnitario();
        validarImporte(errores, "precioUnitario", precioUnitario);
        ValidacionesServicio.lanzarSiHayErrores(errores);

        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setCategoriaId(solicitud.categoriaId());
        producto.setUnidadMedida(solicitud.unidadMedida());
        producto.setStockMinimo(stockMinimo);
        producto.setPrecioUnitario(precioUnitario);
        return producto;
    }

    private static ProductoProveedor validarRelacion(ProductoProveedorRequestDto solicitud) {
        Map<String, String> errores = new LinkedHashMap<>();
        if (solicitud == null || solicitud.proveedorId() == null) {
            errores.put("proveedorId", "Es obligatorio.");
        }
        BigDecimal precioCompra = solicitud == null ? null : solicitud.precioCompra();
        validarImporte(errores, "precioCompra", precioCompra);
        ValidacionesServicio.lanzarSiHayErrores(errores);

        ProductoProveedor relacion = new ProductoProveedor();
        relacion.setProveedorId(solicitud.proveedorId());
        relacion.setPrecioCompra(precioCompra);
        return relacion;
    }

    private static void validarImporte(Map<String, String> errores, String campo, BigDecimal valor) {
        if (valor == null) {
            errores.put(campo, "Es obligatorio.");
        } else if (valor.compareTo(BigDecimal.ZERO) < 0) {
            errores.put(campo, "Debe ser mayor o igual que cero.");
        }
    }

    private static RecursoNoEncontradoException noEncontrado(Long id) {
        return new RecursoNoEncontradoException("No se encontró el producto" + (id == null ? "" : " con id " + id));
    }
}