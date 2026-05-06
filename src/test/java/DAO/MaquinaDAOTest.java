package DAO;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import java.util.List;
import DAO.MaquinaDAO;
import Entidades.MaquinaExpendedora;

@Tag("DAO")
class MaquinaDAOTest {

    private MaquinaDAO dao;

    @BeforeEach
    void setUp() {
        dao = new MaquinaDAO();
    }

    @Test
    @DisplayName("DAO-M-01: Búsqueda por ID (Case Insensitive)")
    void testBuscarPorId() throws Exception {
        MaquinaExpendedora m = new MaquinaExpendedora();
        m.setID("M-001");
        
        dao.insertar(m);
        
        // Probamos que encuentra el ID tanto en mayúsculas como minúsculas
        assertNotNull(dao.buscarPorId("M-001"));
        assertNotNull(dao.buscarPorId("m-001"));
    }

    @Test
    @DisplayName("DAO-M-02: Listar todas devuelve una copia independiente")
    void testListarTodasIntegridad() {
        dao.insertar(new MaquinaExpendedora());
        
        List<MaquinaExpendedora> listaFuera = dao.listarTodas();
        assertEquals(1, listaFuera.size());
        
        // Intentamos modificar la lista que nos ha dado el DAO
        listaFuera.clear();
        
        // La lista interna del DAO debe seguir teniendo su elemento (Inmutabilidad)
        assertEquals(1, dao.listarTodas().size(), "La lista original no debería haberse borrado");
    }

    @Test
    @DisplayName("DAO-M-03: Buscar ID que no existe")
    void testBuscarInexistente() {
        assertNull(dao.buscarPorId("M-999"));
    }
}
