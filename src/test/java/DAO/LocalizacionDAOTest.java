package DAO;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import DAO.LocalizacionDAO;
import Entidades.Localizacion;

@Tag("DAO")
class LocalizacionDAOTest {

    private LocalizacionDAO dao;

    @BeforeEach
    void setUp() {
        // Importante: Cada test empieza con un DAO nuevo (lista vacía)
        dao = new LocalizacionDAO();
    }

    @Test
    @DisplayName("DAO-L-01: Insertar y recuperar por coordenadas exactas")
    void testInsertarYBuscar() throws Exception {
        Localizacion l = new Localizacion();
        l.setLatitud(40.5);
        l.setLongitud(-3.2);
        
        dao.insertar(l);
        
        Localizacion recuperada = dao.buscarPorCoordenadas(40.5, -3.2);
        assertNotNull(recuperada, "Debería encontrar la localización");
        assertEquals(40.5, recuperada.getLatitud());
    }

    @Test
    @DisplayName("DAO-L-02: Retornar null si las coordenadas no existen")
    void testBuscarInexistente() {
        Localizacion recuperada = dao.buscarPorCoordenadas(10.0, 10.0);
        assertNull(recuperada, "No debería encontrar nada en una lista vacía");
    }
}
