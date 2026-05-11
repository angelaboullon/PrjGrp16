package Servicio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import DAO.*;
import Entidades.*;
import Excepciones.*;

@Tag("CajaNegra")
@Tag("HU2")
@DisplayName("Pruebas de Integración - Servicio de Asignación (HU2)")
class VendingServiceHU2Test {

    @Mock private MaquinaDAO maquinaDAO;
    @Mock private ProductoDAO productoDAO;
    
    @InjectMocks
    private VendingService servicio;

    private AutoCloseable openMocks;

    @BeforeEach
    void setup() { 
        openMocks = MockitoAnnotations.openMocks(this); 
    }

    @AfterEach
    void tearDown() throws Exception { 
        openMocks.close(); 
    }

    @Test
    @DisplayName("CP-16: Fallo cuando la máquina no existe en el sistema")
    void testMaquinaInexistente() throws Exception {
      
        String idFicticio = "M-999"; 
        when(maquinaDAO.buscarPorId(idFicticio)).thenReturn(null);
        
        MaquinaExpendedora m = new MaquinaExpendedora();
        m.setID(idFicticio); 
   
        Producto p = new Producto();

        // 2. Act & 3. Assert
        assertThrows(EntityNotFoundException.class, 
            () -> servicio.asignarProductoMaquina(m, p, 10),
            "Debe fallar si la máquina M-999 no está registrada");
    }

    @Test
    @DisplayName("CP-17: Error por producto ya asignado previamente a la máquina")
    void testProductoDuplicadoEnMaquina() throws Exception {
        MaquinaExpendedora m = new MaquinaExpendedora();
        Producto p = new Producto(); 
        p.setId("P-001");
        
        Stock s = new Stock(); 
        s.setProducto(p);
        m.addStock(s); 

        when(maquinaDAO.buscarPorId(any())).thenReturn(m);
        when(productoDAO.buscarPorId(any())).thenReturn(p);

        assertThrows(ProductAlreadyAssignedException.class, 
            () -> servicio.asignarProductoMaquina(m, p, 5),
            "No debe permitir duplicados de producto en la misma máquina");
    }

    @Test
    @DisplayName("CP-18: Éxito al asignar cupo exacto hasta el límite de la máquina")
    void testCupoExactoExito() throws CapacityOutOfRangeException, InvalidIdentifierException {
        // Arrange
        MaquinaExpendedora m = new MaquinaExpendedora();
        m.setCapacidad(100);
        
        
        Producto pPrevio = new Producto();
        pPrevio.setId("P-001"); 
        
        Stock previo = new Stock();
        previo.setProducto(pPrevio); 
        previo.setCapacidadMax(90);
        m.addStock(previo);

        Producto pNuevo = new Producto();
        pNuevo.setId("P-002");

        when(maquinaDAO.buscarPorId(any())).thenReturn(m);
        when(productoDAO.buscarPorId(any())).thenReturn(pNuevo);

        // Act & Assert
        assertDoesNotThrow(() -> servicio.asignarProductoMaquina(m, pNuevo, 10),
            "Debe permitir la asignación si el cupo es igual al espacio libre");
        
        assertEquals(2, m.getListaStock().size(), "El producto debe haberse añadido correctamente");
    }

    @Test
    @DisplayName("CP-19: Error por exceso de capacidad física")
    void testExcesoCapacidad() throws Exception {
        MaquinaExpendedora m = new MaquinaExpendedora();
        m.setCapacidad(10);
        Producto p = new Producto();
        
        when(maquinaDAO.buscarPorId(any())).thenReturn(m);
        when(productoDAO.buscarPorId(any())).thenReturn(p);

        assertThrows(CapacityExceededException.class, 
            () -> servicio.asignarProductoMaquina(m, p, 11),
            "Debe lanzar excepción si el cupo supera la capacidad disponible");
    }

    @Test
    @DisplayName("CP-20: Validación de cupoMax > 0")
    void testCupoInvalido() {
        MaquinaExpendedora m = mock(MaquinaExpendedora.class);
        Producto p = mock(Producto.class);
        
        when(maquinaDAO.buscarPorId(any())).thenReturn(m);
        when(productoDAO.buscarPorId(any())).thenReturn(p);

        assertThrows(IllegalArgumentException.class, 
            () -> servicio.asignarProductoMaquina(m, p, 0),
            "El cupo debe ser estrictamente positivo");
    }
    
    
    @Test
    @DisplayName("CP-21: Alta de producto correcta")
    void testDarAltaExito() throws Exception {
        Producto p = new Producto();
        p.setId("P-005");
        p.setNombre("Zumo");
        
        // El producto no debe existir ni por ID ni por nombre
        when(productoDAO.buscarPorId("P-005")).thenReturn(null);
        when(productoDAO.buscarPorNombre("Zumo")).thenReturn(null);

        assertDoesNotThrow(() -> servicio.darAltaProducto(p));
        verify(productoDAO, times(1)).insertar(p); 
    }

    @Test
    @DisplayName("CP-22: Error por ID de producto duplicado en catálogo")
    void testAltaIdDuplicado() throws Exception {
        Producto p = new Producto();
        p.setId("P-001");
        
        // Simulamos que el ID ya existe
        when(productoDAO.buscarPorId("P-001")).thenReturn(p);

        // Act & Assert 
        assertThrows(DuplicateIdentifierException.class, 
            () -> servicio.darAltaProducto(p), 
            "Debe fallar si el ID ya está en el catálogo global");
    }

    @Test
    @DisplayName("CP-23: Error por nombre de producto duplicado en catálogo")
    void testAltaNombreDuplicado() throws Exception {
        Producto p = new Producto();
        p.setNombre("Agua");
        
        // ID libre pero nombre ocupado
        when(productoDAO.buscarPorId(any())).thenReturn(null);
        when(productoDAO.buscarPorNombre("Agua")).thenReturn(p);

        // Act & Assert
        assertThrows(DuplicateNameException.class, 
            () -> servicio.darAltaProducto(p), 
            "Debe fallar si el nombre ya está registrado");
    }
    
    @Test
    @DisplayName("CP-24: Fallo cuando la máquina existe pero el producto no (Limpia amarillo)")
    void testProductoInexistente() {
        // Simulamos que la máquina SÍ existe pero el producto NO 
        when(maquinaDAO.buscarPorId(anyString())).thenReturn(new MaquinaExpendedora());
        when(productoDAO.buscarPorId(anyString())).thenReturn(null);

        assertThrows(EntityNotFoundException.class, 
            () -> servicio.asignarProductoMaquina(new MaquinaExpendedora(), new Producto(), 10));
    }
    
    @Nested
    @DisplayName("Pruebas de Reposición de Stock (HU2)")
    class ReposicionStockTests {

        @Test
        @DisplayName("CP-28: Reposición exitosa de existencias")
        void testReponerStockExito() throws Exception {
            // Arrange
            MaquinaExpendedora m = new MaquinaExpendedora();
            Producto p = new Producto();
            p.setId("P-001");
            
            Stock s = new Stock();
            s.setProducto(p);
            s.setCapacidadMax(50);
            s.setCantidadActual(10);
            m.addStock(s); // Producto ya asociado

            // Act
            servicio.reponerStock(m, p, 20);

            // Assert
            assertEquals(30, s.getCantidadActual(), "El stock debería haberse incrementado a 30");
            assertNotNull(s.getFechaUltimaReposicion(), "La fecha de reposición debe haberse actualizado");
        }

        @Test
        @DisplayName("CP-29: Fallo al reponer producto que no está en la máquina")
        void testReponerProductoInexistente() throws Exception {
            // Arrange
            MaquinaExpendedora m = new MaquinaExpendedora(); // Máquina sin productos
            Producto p = new Producto();
            p.setId("P-999");

            // Act & Assert
            assertThrows(EntityNotFoundException.class, 
                () -> servicio.reponerStock(m, p, 10),
                "Debe fallar porque el producto no ha sido asociado previamente");
        }
        @Test
        @DisplayName("CP-30: Fallo al reponer cantidad negativa (Límite inferior)")
        void testReponerCantidadNegativa() throws Exception {
            // Arrange
            MaquinaExpendedora m = new MaquinaExpendedora();
            Producto p = new Producto();
            p.setId("P-001");
            
            Stock s = new Stock();
            s.setProducto(p);
            s.setCapacidadMax(50);
            s.setCantidadActual(10);
            m.addStock(s);

            // Act & Assert
            // Verificamos que el sistema bloquea cantidades negativas o cero si así se definió
            assertThrows(IllegalArgumentException.class, 
                () -> servicio.reponerStock(m, p, -5),
                "La cantidad a reponer debe ser un entero positivo");
        }
        @Test
        @DisplayName("CP-31: Fallo por exceso de capacidad en el muelle (AVL)")
        void testReponerExcesoCapacidad() throws Exception{
            MaquinaExpendedora m = new MaquinaExpendedora();
            Producto p = new Producto();
            p.setId("P-001"); 
            
            Stock s = new Stock();
            s.setProducto(p);
            s.setCapacidadMax(20);
            s.setCantidadActual(15);
            m.addStock(s);

            assertThrows(FullCapacityException.class, 
                () -> servicio.reponerStock(m, p, 6),
                "Debe lanzar FullCapacityException al superar el máximo del muelle");
        }
    } 

    @Test
    @Tag("CajaBlanca")
    @DisplayName("CP-CB-01: Forzar segundo operando del OR (Limpia línea 42)")
    void testCajaBlancaProductoNoExiste() throws Exception {
        MaquinaExpendedora m = new MaquinaExpendedora();
        m.setID("M-001"); 
        
        Producto pInexistente = new Producto();
        pInexistente.setId("P-999");

        when(maquinaDAO.buscarPorId("M-001")).thenReturn(m);
        when(productoDAO.buscarPorId("P-999")).thenReturn(null);

        assertThrows(EntityNotFoundException.class, 
            () -> servicio.asignarProductoMaquina(m, pInexistente, 10));
    }
} // Cierra VendingServiceHU2Test}