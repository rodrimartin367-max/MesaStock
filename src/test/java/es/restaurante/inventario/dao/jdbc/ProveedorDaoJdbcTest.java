package es.restaurante.inventario.dao.jdbc;

import es.restaurante.inventario.dao.ProveedorDao;
import es.restaurante.inventario.exception.ConflictoException;
import es.restaurante.inventario.model.Proveedor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProveedorDaoJdbcTest extends JdbcDaoTestSupport {

    private final ProveedorDao dao = new ProveedorDaoJdbc(DATA_SOURCE);

    @Test
    void realizaOperacionesCrudYCompruebaElCif() {
        Proveedor proveedor = nuevoProveedor("Distribuciones Norte", "B1234567X");

        long id = dao.insert(proveedor);

        assertEquals(id, proveedor.getId());
        assertEquals("Distribuciones Norte", dao.findById(id).orElseThrow().getNombre());
        assertEquals(1, dao.findAll().size());
        assertTrue(dao.existsByCif("B1234567X"));
        proveedor.setNombre("Distribuciones Centro");
        proveedor.setCif("A1234567Z");
        assertTrue(dao.update(proveedor));
        assertEquals("A1234567Z", dao.findById(id).orElseThrow().getCif());
        assertFalse(dao.existsByCif("B1234567X"));
        assertTrue(dao.delete(id));
        assertFalse(dao.delete(id));
    }

    @Test
    void devuelveVacioParaIdInexistenteYConflictoParaCifDuplicado() {
        dao.insert(nuevoProveedor("Proveedor Uno", "B1234567X"));

        assertTrue(dao.findById(-1L).isEmpty());
        assertThrows(ConflictoException.class,
                () -> dao.insert(nuevoProveedor("Proveedor Dos", "B1234567X")));
    }

    private static Proveedor nuevoProveedor(String nombre, String cif) {
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre(nombre);
        proveedor.setCif(cif);
        return proveedor;
    }
}