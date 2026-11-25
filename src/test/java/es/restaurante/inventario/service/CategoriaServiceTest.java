package es.restaurante.inventario.service;

import es.restaurante.inventario.dao.CategoriaDao;
import es.restaurante.inventario.dto.CategoriaRequestDto;
import es.restaurante.inventario.exception.ConflictoException;
import es.restaurante.inventario.exception.RecursoNoEncontradoException;
import es.restaurante.inventario.exception.ValidacionException;
import es.restaurante.inventario.model.Categoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaDao categoriaDao;

    private CategoriaService servicio;

    @BeforeEach
    void setUp() {
        servicio = new CategoriaService(categoriaDao);
    }

    @Test
    void creaActualizaListaYBorraCategorias() {
        Categoria categoria = categoria(1L, "Carnes");
        when(categoriaDao.insert(any(Categoria.class))).thenReturn(1L);
        when(categoriaDao.findAll()).thenReturn(List.of(categoria));
        when(categoriaDao.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaDao.update(any(Categoria.class))).thenReturn(true);
        when(categoriaDao.countProductos(1L)).thenReturn(0L);
        when(categoriaDao.delete(1L)).thenReturn(true);

        assertEquals("Carnes", servicio.crear(new CategoriaRequestDto(" Carnes ", null)).nombre());
        assertEquals(1, servicio.listar().size());
        assertEquals("Carnes", servicio.actualizar(1L, new CategoriaRequestDto("Carnes", "Frescas")).nombre());
        servicio.eliminar(1L);

        ArgumentCaptor<Categoria> captor = ArgumentCaptor.forClass(Categoria.class);
        verify(categoriaDao).insert(captor.capture());
        assertEquals("Carnes", captor.getValue().getNombre());
        verify(categoriaDao).delete(1L);
    }

    @Test
    void agrupaErroresDeValidacionPorCampo() {
        ValidacionException excepcion = assertThrows(ValidacionException.class,
                () -> servicio.crear(new CategoriaRequestDto("  ", "x".repeat(256))));

        assertEquals("Es obligatorio.", excepcion.getErrores().get("nombre"));
        assertEquals("No puede superar los 255 caracteres.", excepcion.getErrores().get("descripcion"));
        verify(categoriaDao, never()).insert(any());
    }

    @Test
    void obtenerIdInexistenteLanzaNotFound() {
        when(categoriaDao.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.obtenerPorId(99L));
    }

    @Test
    void noBorraCategoriaConProductos() {
        when(categoriaDao.findById(1L)).thenReturn(Optional.of(categoria(1L, "Carnes")));
        when(categoriaDao.countProductos(1L)).thenReturn(3L);

        ConflictoException excepcion = assertThrows(ConflictoException.class, () -> servicio.eliminar(1L));

        assertEquals("La categoría tiene 3 productos", excepcion.getMessage());
        verify(categoriaDao, never()).delete(1L);
    }

    private static Categoria categoria(Long id, String nombre) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNombre(nombre);
        return categoria;
    }
}