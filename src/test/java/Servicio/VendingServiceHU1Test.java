package Servicio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import DAO.LocalizacionDAO;
import DAO.MaquinaDAO;
import Entidades.Localizacion;
import Entidades.MaquinaExpendedora;
import Excepciones.DuplicateIdentifierException;
import Excepciones.LocationOccupiedException;
import Servicio.VendingService;

class VendingServiceHU1Test {

    // 1. Declaramos los Mocks (Los "dobles de riesgo" de tus DAOs)
    @Mock 
    private MaquinaDAO maquinaDAO;
    
    @Mock 
    private LocalizacionDAO localizacionDAO;

    // 2. Inyectamos los Mocks directamente en el Servicio
    @InjectMocks 
    private VendingService servicio;

    private AutoCloseable openMocks;

    @BeforeEach
    void setUp() {
        // Inicializamos los Mocks antes de cada test para que estén limpios
        openMocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Cerramos los Mocks al terminar para liberar memoria
        openMocks.close();
    }

    // ==========================================
    // PRUEBAS DE LA HU1 (Prueba Unitaria Pura)
    // ==========================================
    @Nested
    @Tag("HU1")
    @DisplayName("HU1: Flujo Completo Alta de Máquina (Con Mocks)")
    class PruebasHU1 {

        @Test
        @DisplayName("CP-13: Alta limpia sin colisiones")
        void testDarAltaMaquina_Exito() throws Exception { 
            MaquinaExpendedora m1 = new MaquinaExpendedora();
            m1.setID("M-001");
            Localizacion l1 = new Localizacion();
            l1.setLatitud(40.0);
            l1.setLongitud(-3.0);

            // ENTRENAMOS AL MOCK: Si te buscan, di que no hay nada (null)
            when(maquinaDAO.buscarPorId("M-001")).thenReturn(null);
            when(localizacionDAO.buscarPorCoordenadas(40.0, -3.0)).thenReturn(null);

            assertDoesNotThrow(() -> servicio.darAltaMaquina(m1, l1));
            
            // VERIFICACIÓN EXTRA: Comprobamos que el servicio intentó guardar la máquina
            verify(maquinaDAO, times(1)).insertar(m1);
        }

        @Test
        @DisplayName("CP-14: Fallo por intento de ID duplicado")
        void testDarAltaMaquina_IDDuplicado() throws Exception { 
            MaquinaExpendedora m2 = new MaquinaExpendedora();
            m2.setID("M-001"); 
            Localizacion l2 = new Localizacion();
            l2.setLatitud(41.0);
            l2.setLongitud(-4.0);

            // ENTRENAMOS AL MOCK: Simulamos que el ID ya existe en la BD devolviendo un objeto cualquiera
            when(maquinaDAO.buscarPorId("M-001")).thenReturn(new MaquinaExpendedora());

            assertThrows(DuplicateIdentifierException.class, () -> servicio.darAltaMaquina(m2, l2));
            
            // VERIFICACIÓN EXTRA: Si saltó la excepción, NUNCA debió llegar a llamar a "insertar"
            verify(maquinaDAO, never()).insertar(any());
        }

        @Test
        @DisplayName("CP-15: Fallo por ubicación geográficamente ocupada")
        void testDarAltaMaquina_UbicacionOcupada() throws Exception { 
            // 1. Preparamos la máquina "fantasma" que ya está ocupando el terreno
            MaquinaExpendedora m1 = new MaquinaExpendedora();
            m1.setID("M-001");
            Localizacion l1 = new Localizacion();
            l1.setLatitud(40.0);
            l1.setLongitud(-3.0);
            m1.setLocalizacion(l1); // OJO: Vital para que el bucle for del servicio no pete

            // 2. Preparamos la nueva máquina que intenta ponerse en el mismo sitio
            MaquinaExpendedora m2 = new MaquinaExpendedora();
            m2.setID("M-002");
            Localizacion l2 = new Localizacion();
            l2.setLatitud(40.0); // Misma latitud
            l2.setLongitud(-3.0); // Misma longitud

            // Cuando el servicio llame a 'listarTodas()', le pasamos la lista con la máquina que molesta
            List<MaquinaExpendedora> listaFalsa = new ArrayList<>();
            listaFalsa.add(m1);
            when(maquinaDAO.listarTodas()).thenReturn(listaFalsa);

            // 4. Verificamos que salta la excepción correcta
            assertThrows(LocationOccupiedException.class, () -> servicio.darAltaMaquina(m2, l2));
            
            // 5. Garantizamos que no se guardó nada por accidente
            verify(maquinaDAO, never()).insertar(any());
        }
        
     // ==========================================
        // PRUEBAS DE CAJA BLANCA (McCabe)
        // ==========================================

        @Test
        @DisplayName("CP-16 (CB): Forzar paso por el catch (Localización ya registrada)")
        void testDarAltaMaquina_CatchLocalizacionExistente() throws Exception { 
            MaquinaExpendedora m1 = new MaquinaExpendedora();
            m1.setID("M-005");
            Localizacion l1 = new Localizacion();
            l1.setLatitud(10.0);
            l1.setLongitud(20.0);

            // Le decimos que esta coordenada YA EXISTE en la BD
            when(localizacionDAO.buscarPorCoordenadas(10.0, 20.0)).thenReturn(new Localizacion());
            
            // Que los demás DAOs den vía libre para que el test termine bien
            when(maquinaDAO.buscarPorId("M-005")).thenReturn(null);
            
            // Ejecutamos. El servicio debe lanzar la excepción internamente, 
            // capturarla en el catch (poniéndolo verde) y continuar hasta guardar la máquina.
            assertDoesNotThrow(() -> servicio.darAltaMaquina(m1, l1));
            
            // Verificamos que efectivamente llegó al final y la guardó
            verify(maquinaDAO, times(1)).insertar(m1);
        }

        @Test
        @DisplayName("CP-17 (CB): Forzar if falso en bucle (Máquinas en otras ubicaciones)")
        void testDarAltaMaquina_BucleConOtrasMaquinas() throws Exception { 
            // 1. La máquina nueva que queremos guardar
            MaquinaExpendedora mNueva = new MaquinaExpendedora();
            mNueva.setID("M-006");
            Localizacion lNueva = new Localizacion();
            lNueva.setLatitud(10.0);
            lNueva.setLongitud(20.0);

            // Una máquina "vecina" que está en OTRA ubicación
            MaquinaExpendedora mVecina = new MaquinaExpendedora();
            Localizacion lVecina = new Localizacion();
            lVecina.setLatitud(80.0); // Coordenada diferente
            lVecina.setLongitud(80.0); // Coordenada diferente
            mVecina.setLocalizacion(lVecina);

            // Simulamos que al listar, la base de datos devuelve a la vecina
            List<MaquinaExpendedora> listaFalsa = new ArrayList<>();
            listaFalsa.add(mVecina);
            when(maquinaDAO.listarTodas()).thenReturn(listaFalsa);

            // Vía libre en el resto
            when(localizacionDAO.buscarPorCoordenadas(10.0, 20.0)).thenReturn(null);
            when(maquinaDAO.buscarPorId("M-006")).thenReturn(null);

            // Ejecutamos. El bucle dará una vuelta, el 'if' dirá "no chocan" (False -> Verde) 
            // y la máquina nueva se guardará con éxito.
            assertDoesNotThrow(() -> servicio.darAltaMaquina(mNueva, lNueva));
        }
        
        @Test
        @Tag("Robustez")
        @DisplayName("CP-18: Protección contra localización nula (Mocks)")
        void testDarAltaMaquina_LocalizacionNula() throws Exception {
            // Arrange
            MaquinaExpendedora mValida = new MaquinaExpendedora();
            mValida.setID("M-001");
            Localizacion lNula = null;

            // Act & Assert
            // Al ser 'mValida' no nulo, forzamos a Java a evaluar la segunda parte del OR (l == null)
            assertThrows(IllegalArgumentException.class, () -> {
                servicio.darAltaMaquina(mValida, lNula);
            }, "El servicio debe detectar que la localización es nula y lanzar IllegalArgumentException");
            
            // Verificación: Aseguramos que no se llamó a ningún DAO al fallar la validación inicial
            verify(maquinaDAO, never()).insertar(any());
            verify(localizacionDAO, never()).buscarPorCoordenadas(anyDouble(), anyDouble());
        }
        
        @Test
        @Tag("Robustez")
        @DisplayName("CP-19: Protección máquina nula (Mocks)")
        void testDarAltaMaquina_MaquinaNula() throws Exception {
            // Arrange
            // Preparamos una localización válida pero una máquina nula
            Localizacion lValida = new Localizacion();
            lValida.setLatitud(42.88); 
            lValida.setLongitud(-8.54);
            MaquinaExpendedora mNula = null;

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                servicio.darAltaMaquina(mNula, lValida);
            }, "El servicio debe detectar que la máquina es nula antes de operar");

            // Verification (Caja Negra/Blanca con Mocks)
            // Verificamos que NUNCA se llamó al método insertar del DAO
            verify(maquinaDAO, never()).insertar(any());
        }
    }
}