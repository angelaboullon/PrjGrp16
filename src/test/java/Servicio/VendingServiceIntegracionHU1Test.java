package Servicio;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import DAO.LocalizacionDAO;
import DAO.MaquinaDAO;
import Entidades.Localizacion;
import Entidades.MaquinaExpendedora;
import Servicio.VendingService;
import Excepciones.*;

@Tag("Integracion")
@DisplayName("Pruebas de Integración - HU1: Gestión de Máquinas")
class VendingServiceIntegracionHU1Test {

    private VendingService servicio;
    private MaquinaDAO maquinaDAO;
    private LocalizacionDAO localizacionDAO;

    @BeforeEach
    void setUp() {
        maquinaDAO = new MaquinaDAO();
        localizacionDAO = new LocalizacionDAO();
        servicio = new VendingService(maquinaDAO, localizacionDAO, null, null);
    }

    @Test
    @DisplayName("INT-01: Flujo completo de Alta Exitosa")
    void testIntegracionAltaExitosa() throws Exception {
        MaquinaExpendedora m = new MaquinaExpendedora();
        m.setID("M-001"); 
        Localizacion l = new Localizacion();
        l.setLatitud(10.0); l.setLongitud(10.0);

        assertDoesNotThrow(() -> servicio.darAltaMaquina(m, l));

        assertNotNull(maquinaDAO.buscarPorId("M-001"));
    }

    @Test
    @DisplayName("INT-02: Fallo por ID Duplicado (Evitando NullPointerException)")
    void testIntegracionErrorIdDuplicado() throws Exception {
        // 1. Creamos una máquina previa COMPLETA (con localización)
        MaquinaExpendedora mPrevia = new MaquinaExpendedora();
        mPrevia.setID("M-111");
        Localizacion lPrevia = new Localizacion();
        lPrevia.setLatitud(80.0); lPrevia.setLongitud(80.0);
        mPrevia.setLocalizacion(lPrevia); // <--- ESTO EVITA EL NULLPOINTEREXCEPTION
        
        maquinaDAO.insertar(mPrevia);

        // 2. Intentamos dar de alta otra con el mismo ID pero distinta ubicación
        MaquinaExpendedora mNueva = new MaquinaExpendedora();
        mNueva.setID("M-111"); 
        Localizacion lNueva = new Localizacion();
        lNueva.setLatitud(20.0); lNueva.setLongitud(20.0);

        // Ahora el servicio podrá recorrer la lista sin explotar y llegará al check del ID
        assertThrows(DuplicateIdentifierException.class, () -> {
            servicio.darAltaMaquina(mNueva, lNueva);
        });
    }

    @Test
    @DisplayName("INT-03: Fallo por Ubicación Ocupada")
    void testIntegracionErrorUbicacionOcupada() throws Exception {
        Localizacion lOcupada = new Localizacion();
        lOcupada.setLatitud(40.0); lOcupada.setLongitud(40.0);
        
        MaquinaExpendedora mExistente = new MaquinaExpendedora();
        mExistente.setID("M-222");
        mExistente.setLocalizacion(lOcupada); // Máquina completa
        maquinaDAO.insertar(mExistente);

        MaquinaExpendedora mNueva = new MaquinaExpendedora();
        mNueva.setID("M-333");

        assertThrows(LocationOccupiedException.class, () -> {
            servicio.darAltaMaquina(mNueva, lOcupada);
        });
    }

    @Test
    @DisplayName("INT-04: Reutilización de localización existente")
    void testIntegracionReutilizarLocalizacion() throws Exception {
        Localizacion lExistente = new Localizacion();
        lExistente.setLatitud(50.0); lExistente.setLongitud(50.0);
        localizacionDAO.insertar(lExistente);

        MaquinaExpendedora m = new MaquinaExpendedora();
        m.setID("M-444");

        assertDoesNotThrow(() -> servicio.darAltaMaquina(m, lExistente));
        assertEquals(m, maquinaDAO.buscarPorId("M-444"));
    }
}