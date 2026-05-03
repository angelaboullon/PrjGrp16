package Servicio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDate;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import DAO.*;
import Entidades.*;
import Excepciones.*;

@Tag("HU2")
@DisplayName("HU2: Gestión de Inventario y Reposición")
class VendingServiceHU2Test {

    @Mock MaquinaDAO maquinaDAO;
    @Mock ProductoDAO productoDAO;
    @Mock VentaDAO ventaDAO;
    @InjectMocks VendingService service;

    AutoCloseable openMocks;

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    // --- PRUEBA UNITARIA: PR-UN-03 / PR-UN-05 (Atributos de Producto) ---
    @Nested
    @DisplayName("Validación de Atributos de Producto")
    class ValidarAtributosProducto {

        @Test
        @DisplayName("PR-UN-03: Validación de precio, nombre y fecha")
        void testValidacionesProducto() {
            Producto p = new Producto();
            assertAll("Atributos inválidos",
                // CP-12: Precio negativo (según tu tabla de Subprueba 2)
                () -> assertThrows(InvalidPriceException.class, () -> p.setPrecio(-5.0), "CP-12"),
                // CP-13: Precio cero
                () -> assertThrows(InvalidPriceException.class, () -> p.setPrecio(0.0), "CP-13"),
                // CP-27: Nombre corto (según tu tabla de setNombre)
                () -> assertThrows(MalformedNameException.class, () -> p.setNombre("Ab"), "CP-27"),
                // CP-29: Fecha futura (según tu tabla de setFechaAlta)
                () -> assertThrows(FutureDateException.class, () -> p.setFechaAlta(LocalDate.now().plusDays(1)), "CP-29")
            );
        }

        @Test
        @DisplayName("PR-UN-05: Validación de Categoría (CP-23, CP-24)")
        void testValidacionCategoria() {
            Producto p = new Producto();
            assertAll("Categorías inválidas",
                () -> assertThrows(IllegalArgumentException.class, () -> p.setCategoria("Ab"), "CP-23"),
                () -> assertThrows(IllegalArgumentException.class, () -> p.setCategoria(null), "CP-24")
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {"P-001", "P-123", "P-999"})
        @DisplayName("CP-08: Formato Válido ID de Producto")
        void testIdsValidos(String id) throws Exception { // throws necesario para setId
            Producto p = new Producto();
            assertDoesNotThrow(() -> p.setId(id));
        }

        @Test
        @DisplayName("CP-09, CP-10: IDs Inválidos")
        void testIdsInvalidos() {
            Producto p = new Producto();
            assertAll("IDs erróneos",
                () -> assertThrows(InvalidIdentifierException.class, () -> p.setId("M-001"), "CP-09"),
                () -> assertThrows(InvalidIdentifierException.class, () -> p.setId("P-1"), "CP-10")
            );
        }
    }

    // --- PRUEBA DE INTEGRACIÓN: PR-INT-02 / PR-INT-05 (Asignación) ---
    @Nested
    @DisplayName("Asignación de Stock y Capacidad")
    class AsignacionStockIntegration {

        @Test
        @DisplayName("CP-15: Escenario Ideal (Asignación válida)")
        void testAsignacionExito() throws Exception {
            MaquinaExpendedora m = new MaquinaExpendedora();
            m.setID("M-001"); m.setCapacidad(100);
            Producto p = new Producto(); p.setId("P-001");

            when(maquinaDAO.buscarPorId("M-001")).thenReturn(m);
            when(productoDAO.buscarPorId("P-001")).thenReturn(p);

            service.asignarProductoMaquina(m, p, 50);
            assertEquals(1, m.getListaStock().size());
        }

        @Test
        @DisplayName("PR-INT-05: Límite Exacto de Capacidad (BVA)")
        void testLimiteExactoCapacidad() throws Exception {
            MaquinaExpendedora m = new MaquinaExpendedora();
            m.setID("M-001"); m.setCapacidad(100);
            Producto p = new Producto(); p.setId("P-001");

            when(maquinaDAO.buscarPorId("M-001")).thenReturn(m);
            when(productoDAO.buscarPorId("P-001")).thenReturn(p);

            assertDoesNotThrow(() -> service.asignarProductoMaquina(m, p, 100));
        }

        @Test
        @DisplayName("CP-18 / PR-INT-04: Error si el producto no existe")
        void testEntidadInexistente() throws Exception {
            when(productoDAO.buscarPorId(anyString())).thenReturn(null);
            assertThrows(EntityNotFoundException.class, () -> {
                service.asignarProductoMaquina(new MaquinaExpendedora(), new Producto(), 10);
            });
        }
        
        @Test
        @DisplayName("CP-16: Error si el producto ya está asignado")
        void testProductoDuplicado() throws Exception {
            MaquinaExpendedora m = new MaquinaExpendedora();
            Producto p = new Producto(); p.setId("P-001");
            Stock s = new Stock(); s.setProducto(p);
            m.addStock(s);

            when(maquinaDAO.buscarPorId(any())).thenReturn(m);
            when(productoDAO.buscarPorId(any())).thenReturn(p);

            assertThrows(ProductAlreadyAssignedException.class, () -> 
                service.asignarProductoMaquina(m, p, 10));
        }
    }

    // --- PRUEBA DE INTEGRACIÓN: REPOSICIÓN ---
    @Nested
    @DisplayName("Reposición de Stock")
    class ReposicionTests {

        @Test
        @DisplayName("CP-19: Reposición válida")
        void testReponerExito() throws Exception {
            Producto p = new Producto(); p.setId("P-001");
            Stock s = new Stock(); s.setProducto(p); s.setCapacidadMax(50); s.setCantidadActual(10);
            MaquinaExpendedora m = new MaquinaExpendedora(); m.addStock(s);

            service.reponerStock(m, p, 20);
            assertEquals(30, s.getCantidadActual());
        }

        @Test
        @DisplayName("CP-20: Error - Desbordamiento de muelle (cupoMax)")
        void testDesbordamientoMuelle() throws Exception {
            Producto p = new Producto(); p.setId("P-001");
            Stock s = new Stock(); 
            s.setCapacidadMax(10); 
            s.setCantidadActual(8);
            s.setProducto(p);
            MaquinaExpendedora m = new MaquinaExpendedora(); m.addStock(s);

            assertThrows(FullCapacityException.class, () -> 
                service.reponerStock(m, p, 5));
        }

        @Test
        @DisplayName("CP-21 / CP-25: Error - Producto no vinculado")
        void testReponerNoVinculado() throws Exception { 
            MaquinaExpendedora m = new MaquinaExpendedora(); //Maquina expendedora vacía
            Producto p = new Producto(); 
            p.setId("P-001"); 

            assertThrows(EntityNotFoundException.class, () -> 
                service.reponerStock(m, p, 5));
        }
    }
}