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
    
    @Test
    @DisplayName("INT-05: Cobertura de ramas (Varios elementos)")
    void testCoberturaRamasBusqueda() throws Exception {
        // 1. Preparamos dos máquinas completas
        MaquinaExpendedora m1 = new MaquinaExpendedora();
        m1.setID("M-001");
        Localizacion l1 = new Localizacion();
        l1.setLatitud(10.0); l1.setLongitud(10.0);
        m1.setLocalizacion(l1);

        MaquinaExpendedora m2 = new MaquinaExpendedora();
        m2.setID("M-002");
        Localizacion l2 = new Localizacion();
        l2.setLatitud(20.0); l2.setLongitud(20.0);
        m2.setLocalizacion(l2);

        // 2. Insertamos AMBAS en los DAOs
        maquinaDAO.insertar(m1);
        maquinaDAO.insertar(m2);
        localizacionDAO.insertar(l1);
        localizacionDAO.insertar(l2);

        // 3. BUSCAMOS LA SEGUNDA (esto fuerza a que la primera sea 'false' en el if)
        // Si falla aquí, revisa que maquinaDAO.buscarPorId use el mismo getter que setID
        MaquinaExpendedora encontrada = maquinaDAO.buscarPorId("M-002");
        assertNotNull(encontrada, "La máquina M-002 debería existir en el DAO");
        assertEquals("M-002", encontrada.getId()); // Usa aquí el mismo nombre que tengas en tu entidad

        // 4. BUSCAMOS LA SEGUNDA LOCALIZACIÓN
        Localizacion locEncontrada = localizacionDAO.buscarPorCoordenadas(20.0, 20.0);
        assertNotNull(locEncontrada, "La localización 20.0, 20.0 debería existir");
    }

    @Test
    @DisplayName("INT-06: Cobertura de rama no encontrado")
    void testBusquedaNoEncontrada() {
        // Buscamos algo que no existe para que el bucle termine y llegue al 'return null'
        assertNull(maquinaDAO.buscarPorId("M-999"));
        assertNull(localizacionDAO.buscarPorCoordenadas(99.9, 99.9));
    }
    
    @Test
    @DisplayName("INT-07: Cobertura total de Double.compare (Cortocircuito)")
    void testCoberturaTotalCoordenadas() throws Exception {
        // 1. Insertamos una localización base
    	// En lugar de: Localizacion l1 = new Localizacion(10.0, 10.0);
    	Localizacion l1 = new Localizacion();
    	l1.setLatitud(10);
    	l1.setLongitud(10.0);
        localizacionDAO.insertar(l1);

        // 2. CASO A: Latitud coincide, pero Longitud NO (Fuerza evaluar la 2ª parte como FALSE)
        assertNull(localizacionDAO.buscarPorCoordenadas(10.0, 99.9), 
            "Debe fallar porque la longitud es distinta");

        // 3. CASO B: Latitud NO coincide (Fuerza el cortocircuito, la longitud ni se mira)
        assertNull(localizacionDAO.buscarPorCoordenadas(99.9, 10.0), 
            "Debe fallar porque la latitud es distinta");

        // 4. CASO C: Coincidencia total (Asegura el TRUE && TRUE)
        assertNotNull(localizacionDAO.buscarPorCoordenadas(10.0, 10.0));
    }
    
    @Test
    @Tag("Integracion")
    @Tag("Robustez")
    @DisplayName("INT-08: Protección contra máquina nula en alta")
    void testAltaMaquinaNula() throws Exception {
        // Arrange
        Localizacion locValida = new Localizacion();
        locValida.setLatitud(10.0);
        locValida.setLongitud(10.0);
        MaquinaExpendedora maquinaNula = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            // Suponiendo que 'servicio' se inicializa en tu @BeforeEach
            servicio.darAltaMaquina(maquinaNula, locValida);
        }, "El servicio debe lanzar IllegalArgumentException al recibir un objeto nulo, evitando que el programa colapse con NullPointerException");
    }
    
    @Test
    @Tag("Integracion")
    @Tag("Robustez")
    @DisplayName("INT-09: Protección localización nula en alta")
    void testAltaLocalizacionNula() throws Exception {
        // Arrange
        // Creamos una máquina válida para que el 'if' del servicio pase la primera condición
        MaquinaExpendedora maquinaValida = new MaquinaExpendedora();
        maquinaValida.setID("M-999");
        Localizacion locNula = null;

        // Act & Assert
        // Java se ve obligado a evaluar (l == null) porque la máquina sí existe
        assertThrows(IllegalArgumentException.class, () -> {
            servicio.darAltaMaquina(maquinaValida, locNula);
        }, "El servicio debe lanzar IllegalArgumentException al recibir una localización nula");
    }
    
    @Test
    @DisplayName("INT-10: Cobertura técnica de seguridad en Entidad (setLocalizacion)")
    void testCoberturaTecnicaEntidad() {
        // Para llegar al 100% en la entidad MaquinaExpendedora, 
        // necesitamos saltarnos el "escudo" del servicio y llamar directamente al setter.
        MaquinaExpendedora m = new MaquinaExpendedora();
        
        // Esto obligará a entrar en la rama del 'if (l == null)' de la entidad
        assertThrows(IllegalArgumentException.class, () -> {
            m.setLocalizacion(null);
        }, "Verificando la protección interna de la entidad MaquinaExpendedora");
    }
    
}