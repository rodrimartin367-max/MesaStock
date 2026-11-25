package es.restaurante.inventario.service;

import es.restaurante.inventario.dao.ProveedorDao;
import es.restaurante.inventario.dto.ProveedorDto;
import es.restaurante.inventario.dto.ProveedorRequestDto;
import es.restaurante.inventario.exception.RecursoNoEncontradoException;
import es.restaurante.inventario.mapper.ProveedorMapper;
import es.restaurante.inventario.model.Proveedor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public final class ProveedorService {

    private static final Pattern FORMATO_CIF = Pattern.compile("^[A-Z]\\d{7}[A-Z0-9]$");
    private static final Pattern FORMATO_EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern FORMATO_TELEFONO = Pattern.compile("^[0-9 +]*$");

    private final ProveedorDao proveedorDao;

    public ProveedorService(ProveedorDao proveedorDao) {
        this.proveedorDao = Objects.requireNonNull(proveedorDao);
    }

    public List<ProveedorDto> listar() {
        return proveedorDao.findAll().stream().map(ProveedorMapper::aDto).toList();
    }

    public ProveedorDto obtenerPorId(Long id) {
        return ProveedorMapper.aDto(buscar(id));
    }

    public ProveedorDto crear(ProveedorRequestDto solicitud) {
        Proveedor proveedor = validar(solicitud);
        proveedor.setId(proveedorDao.insert(proveedor));
        return ProveedorMapper.aDto(proveedor);
    }

    public ProveedorDto actualizar(Long id, ProveedorRequestDto solicitud) {
        buscar(id);
        Proveedor proveedor = validar(solicitud);
        proveedor.setId(id);
        if (!proveedorDao.update(proveedor)) {
            throw noEncontrado(id);
        }
        return ProveedorMapper.aDto(proveedor);
    }

    public void eliminar(Long id) {
        buscar(id);
        if (!proveedorDao.delete(id)) {
            throw noEncontrado(id);
        }
    }

    private Proveedor buscar(Long id) {
        if (id == null) {
            throw noEncontrado(null);
        }
        return proveedorDao.findById(id).orElseThrow(() -> noEncontrado(id));
    }

    private static Proveedor validar(ProveedorRequestDto solicitud) {
        Map<String, String> errores = new LinkedHashMap<>();
        String nombre = solicitud == null ? null
                : ValidacionesServicio.texto(errores, "nombre", solicitud.nombre(), 100, true);
        String cif = solicitud == null ? null
                : ValidacionesServicio.texto(errores, "cif", solicitud.cif(), 9, true);
        if (cif != null) {
            cif = cif.toUpperCase(Locale.ROOT);
            if (!FORMATO_CIF.matcher(cif).matches()) {
                errores.put("cif", "Formato de CIF no válido.");
            }
        }
        String telefono = solicitud == null ? null
                : ValidacionesServicio.texto(errores, "telefono", solicitud.telefono(), 20, false);
        if (telefono != null && !FORMATO_TELEFONO.matcher(telefono).matches()) {
            errores.put("telefono", "Solo se permiten dígitos, espacios y el signo +.");
        }
        String email = solicitud == null ? null
                : ValidacionesServicio.texto(errores, "email", solicitud.email(), 120, false);
        if (email != null && !FORMATO_EMAIL.matcher(email).matches()) {
            errores.put("email", "Formato de email no válido.");
        }
        if (solicitud == null) {
            errores.put("nombre", "Es obligatorio.");
            errores.put("cif", "Es obligatorio.");
        }
        ValidacionesServicio.lanzarSiHayErrores(errores);

        Proveedor proveedor = new Proveedor();
        proveedor.setNombre(nombre);
        proveedor.setCif(cif);
        proveedor.setTelefono(telefono);
        proveedor.setEmail(email);
        return proveedor;
    }

    private static RecursoNoEncontradoException noEncontrado(Long id) {
        return new RecursoNoEncontradoException("No se encontró el proveedor" + (id == null ? "" : " con id " + id));
    }
}