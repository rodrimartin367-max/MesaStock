package es.restaurante.inventario.service;

import es.restaurante.inventario.dao.ProveedorDao;
import es.restaurante.inventario.dto.ProveedorRequestDto;
import es.restaurante.inventario.exception.ValidacionException;
import es.restaurante.inventario.model.Proveedor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProveedorServiceTest {

    @Mock
    private ProveedorDao proveedorDao;

    private ProveedorService servicio;

    @BeforeEach
    void setUp() {
        servicio = new ProveedorService(proveedorDao);
    }

    @Test
    void creaListaActualizaYBorraProveedores() {
        when(proveedorDao.insert(any(Proveedor.class))).thenReturn(4L);
        when(proveedorDao.findAll()).thenReturn(List.of(proveedor(4L)));
        when(proveedorDao.findById(4L)).thenReturn(java.util.Optional.of(proveedor(4L)));
        when(proveedorDao.update(any(Proveedor.class))).thenReturn(true);
        when(proveedorDao.delete(4L)).thenReturn(true);

        var creado = servicio.crear(new ProveedorRequestDto(" Proveedor ", "b1234567c", " +34 123 ", "a@b.es"));
        assertEquals(4L, creado.id());
        assertEquals("B1234567C", creado.cif());
        assertEquals(1, servicio.listar().size());
        assertEquals("Proveedor", servicio.actualizar(4L,
                new ProveedorRequestDto("Proveedor", "B1234567C", null, null)).nombre());
        servicio.eliminar(4L);
        verify(proveedorDao).delete(4L);
    }

    @Test
    void informaTodosLosCamposInvalidos() {
        ValidacionException excepcion = assertThrows(ValidacionException.class,
                () -> servicio.crear(new ProveedorRequestDto(" ", "123", "teléfono", "correo-inválido")));

        assertEquals(4, excepcion.getErrores().size());
        assertEquals("Formato de CIF no válido.", excepcion.getErrores().get("cif"));
        assertEquals("Solo se permiten dígitos, espacios y el signo +.", excepcion.getErrores().get("telefono"));
        assertEquals("Formato de email no válido.", excepcion.getErrores().get("email"));
    }

    private static Proveedor proveedor(Long id) {
        Proveedor proveedor = new Proveedor();
        proveedor.setId(id);
        proveedor.setNombre("Proveedor");
        proveedor.setCif("B1234567C");
        return proveedor;
    }
}