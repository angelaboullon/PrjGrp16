package Servicio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import DAO.VentaDAO;
import Entidades.MaquinaExpendedora;
import Entidades.Producto;
import Entidades.Stock;
import Entidades.Venta;

@DisplayName("PR-HU6-01: Cálculo de Fecha Límite de Reposición")
class VendingServiceHU6Test {

    AutoCloseable acl;

    @Mock
    VentaDAO ventaDAO;

    @InjectMocks
    VendingService servicio;

    MaquinaExpendedora maquina;
    Producto producto;
    Stock stock;

    @BeforeEach
    void setUp() throws Exception {
        acl = MockitoAnnotations.openMocks(this);
        
        maquina = new MaquinaExpendedora();
        maquina.setID("M-100");

        // Recuerda: ID válido de 3 dígitos para evitar InvalidIdentifierException
        producto = new Producto();
        producto.setId("P-099"); 
        producto.setNombre("Barrita Energética");

        stock = new Stock();
        stock.setProducto(producto);
        stock.setCantidadActual(50); 
    }

    @AfterEach
    void tearDown() throws Exception {
        acl.close();
    }

    // =================================================================
    // FASE 1: CAJA NEGRA (Funcionalidad principal)
    // =================================================================

    @Test
    @Tag("CajaNegra")
    @DisplayName("CP-61: Consumo Normal (Proyección estándar)")
    void testEstimarFechaReposicion_ConsumoNormal() {
        stock.setFechaUltimaReposicion(LocalDate.now().minusDays(10));
        maquina.addStock(stock);

        List<Venta> listaVentas = new ArrayList<>();
        listaVentas.add(new Venta("M-100", "P-099", 15, LocalDateTime.now().minusDays(5)));
        listaVentas.add(new Venta("M-100", "P-099", 15, LocalDateTime.now().minusDays(2)));

        when(ventaDAO.buscarDesdeFecha(eq("M-100"), eq("P-099"), any(LocalDateTime.class)))
            .thenReturn(listaVentas);

        LocalDate fechaCalculada = servicio.estimarFechaReposicion(maquina, producto);

        //30 uds en 10 días = 3/día. 50/3 = 16 días. 16 - 1 (margen) = 15 días desde hoy.
        LocalDate fechaEsperada = LocalDate.now().plusDays(15);
        assertEquals(fechaEsperada, fechaCalculada);
        
        verify(ventaDAO, times(1)).buscarDesdeFecha(anyString(), anyString(), any(LocalDateTime.class));
    }

    @Test
    @Tag("CajaNegra")
    @DisplayName("CP-62: Producto No Asignado (Retorna Null)")
    void testEstimarFechaReposicion_ProductoNoExiste() {
        //NO añadimos el stock a la máquina (está vacía)

        LocalDate fechaCalculada = servicio.estimarFechaReposicion(maquina, producto);

        assertNull(fechaCalculada, "Si el producto no está asignado a la máquina, debe devolver null.");
        verify(ventaDAO, never()).buscarDesdeFecha(anyString(), anyString(), any(LocalDateTime.class));
    }
    
    //-------------------------------
    //Tras conseguir un 87% de cobertura con Eclemma en el método estimarFechaReposición
    //codificaremos nuevas pruebas de caja blanca que abarquen todo el código para llegar al 100%
    //-------------------------------
    
    @Test
    @Tag("CajaBlanca")
    @DisplayName("CP-63: Camino A - Control de Nulos (IllegalArgumentException)")
    void testEstimarFechaReposicion_CajaBlanca_CaminoA_Nulos() {
        // Act & Assert (Evaluamos las dos condiciones del OR lógico)
        assertThrows(IllegalArgumentException.class, () -> {
            servicio.estimarFechaReposicion(null, producto); // m == null
        }, "Debe protegerse contra máquinas nulas.");
        
        assertThrows(IllegalArgumentException.class, () -> {
            servicio.estimarFechaReposicion(maquina, null);  // p == null
        }, "Debe protegerse contra productos nulos.");
    }

    @Test
    @Tag("CajaBlanca")
    @DisplayName("CP-64: Camino B - Límite Temporal (Reposición Hoy)")
    void testEstimarFechaReposicion_CajaBlanca_CaminoB_ReposicionHoy() {
        // Simulamos que se rellenó HOY para forzar la condición 'dias == 0'
        stock.setFechaUltimaReposicion(LocalDate.now()); 
        maquina.addStock(stock);

        // Simulamos ventas para asegurar que 'velocidad != 0' y aislar la rama de días
        List<Venta> listaVentas = new ArrayList<>();
        listaVentas.add(new Venta("M-100", "P-099", 5, LocalDateTime.now()));

        when(ventaDAO.buscarDesdeFecha(eq("M-100"), eq("P-099"), any(LocalDateTime.class)))
            .thenReturn(listaVentas);

        LocalDate fechaCalculada = servicio.estimarFechaReposicion(maquina, producto);

        // El código fuerza dias=1. Velocidad = 5/1 = 5. Días para agotar = 50/5 = 10.
        // Fecha = Hoy + 10 - 1 = Hoy + 9.
        LocalDate fechaEsperada = LocalDate.now().plusDays(9);
        assertEquals(fechaEsperada, fechaCalculada, "El sistema no forzó 'dias = 1' correctamente ante una reposición reciente.");
    }

    @Test
    @Tag("CajaBlanca")
    @DisplayName("CP-65: Camino C - Producto Estancado (ArithmeticException)")
    void testEstimarFechaReposicion_CajaBlanca_CaminoC_SinConsumo() {
        // Simulamos que han pasado días para aislar esta condición de la anterior
        stock.setFechaUltimaReposicion(LocalDate.now().minusDays(10));
        maquina.addStock(stock);

        // El Mock devuelve una lista vacía, forzando la condición 'velocidad == 0'
        List<Venta> listaVentasVacia = new ArrayList<>();
        
        when(ventaDAO.buscarDesdeFecha(anyString(), anyString(), any(LocalDateTime.class)))
            .thenReturn(listaVentasVacia);

        ArithmeticException excepcion = assertThrows(ArithmeticException.class, () -> {
            servicio.estimarFechaReposicion(maquina, producto);
        });
        
        assertEquals("Sin datos de rotación.", excepcion.getMessage(), 
                     "El mensaje de la excepción no coincide con el especificado en el diseño.");
    }

}