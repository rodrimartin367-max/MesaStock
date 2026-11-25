package es.restaurante.inventario.dao.jdbc;

import es.restaurante.inventario.dao.CategoriaDao;
import es.restaurante.inventario.exception.ConflictoException;
import es.restaurante.inventario.model.Categoria;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoriaDaoJdbcTest extends JdbcDaoTestSupport {

    private final CategoriaDao dao = new CategoriaDaoJdbc(DATA_SOURCE);

    @Test
    void realizaOperacionesCrud() {
        Categoria categoria = new Categoria();
        categoria.setNombre("Carnes");
        categoria.setDescripcion("Carnes frescas");

        long id = dao.insert(categoria);

        assertEquals(id, categoria.getId());
        assertEquals("Carnes", dao.findById(id).orElseThrow().getNombre());
        assertEquals(1, dao.findAll().size());
        categoria.setNombre("Carnes rojas");
        categoria.setDescripcion("Cortes para cocina");
        assertTrue(dao.update(categoria));
        assertEquals("Carnes rojas", dao.findById(id).orElseThrow().getNombre());
        assertTrue(dao.existsByNombre("Carnes rojas"));
        assertEquals(0, dao.countProductos(id));
        assertTrue(dao.delete(id));
        assertFalse(dao.delete(id));
    }

    @Test
    void devuelveVacioParaIdInexistenteYConflictoParaNombreDuplicado() {
        Categoria categoria = new Categoria();
        categoria.setNombre("Bebidas");
        dao.insert(categoria);

        assertTrue(dao.findById(-1L).isEmpty());
        assertThrows(ConflictoException.class, () -> dao.insert(nuevaCategoria("Bebidas")));
    }

    @Test
    void noBorraCategoriaConProductosYCuentaSusDependencias() {
        long categoriaId = insertarCategoria("Verduras");
        insertarProducto(categoriaId, "Tomate", "4.00", "5.00");

        assertEquals(1, dao.countProductos(categoriaId));
        assertThrows(ConflictoException.class, () -> dao.delete(categoriaId));
    }

    private static Categoria nuevaCategoria(String nombre) {
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        return categoria;
    }
}