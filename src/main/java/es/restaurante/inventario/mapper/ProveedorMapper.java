package es.restaurante.inventario.mapper;

import es.restaurante.inventario.dto.ProveedorDto;
import es.restaurante.inventario.dto.ProveedorRequestDto;
import es.restaurante.inventario.model.Proveedor;

public final class ProveedorMapper {

    private ProveedorMapper() {
    }

    public static ProveedorDto aDto(Proveedor proveedor) {
        return new ProveedorDto(proveedor.getId(), proveedor.getNombre(), proveedor.getCif(),
                proveedor.getTelefono(), proveedor.getEmail());
    }

    public static Proveedor desdeRequestDto(ProveedorRequestDto dto) {
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre(dto.nombre());
        proveedor.setCif(dto.cif());
        proveedor.setTelefono(dto.telefono());
        proveedor.setEmail(dto.email());
        return proveedor;
    }
}