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

import DAO.VentaDAO;
import Entidades.MaquinaExpendedora;
import Entidades.Producto;
import Entidades.Stock;
import Entidades.Venta;

@DisplayName("PR-HU6-INT: Pruebas de Integración del Algoritmo Predictivo")
class VendingServiceIntegracionHU6Test {

    // Instancia real del servicio (SUT)
    private VendingService servicio;
    
    // Colaborador de persistencia integrado
    private VentaDAO ventaDAO;

    // Fixtures reales del modelo de dominio para la línea base del test
    private MaquinaExpendedora maquina;
    private Producto producto;
    private Stock stock;

    @BeforeEach
    void setUp() throws Exception {
        // Para las pruebas de integración, el DAO actúa como un almacén pasivo de datos.
        // Simulamos su comportamiento de consulta sin alterar la lógica de negocio del SUT.
        ventaDAO = mock(VentaDAO.class);
        
        // Inyección de dependencias real a través del constructor del servicio
        servicio = new VendingService(null, null, null, ventaDAO);
        
        // Instanciación completa de las entidades reales involucradas en la funcionalidad
        maquina = new MaquinaExpendedora();
        maquina.setID("M-777");

        producto = new Producto();
        producto.setId("P-777");
        producto.setNombre("Refresco de Cola");

        stock = new Stock();
        stock.setProducto(producto);
        stock.setCantidadActual(40);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Garantizamos que el entorno de pruebas no modifica el estado del sistema de forma persistente
        servicio = null;
        ventaDAO = null;
    }

    @Test
    @Tag("Integracion")
    @DisplayName("CP-66: Integración de Historial Disperso (Suma e Interacción de Capas)")
    void testIntegracion_HistorialDisperso() {
        stock.setFechaUltimaReposicion(LocalDate.now().minusDays(5));
        maquina.addStock(stock);

        // Población del listado de transacciones simulando el comportamiento de la BD real
        List<Venta> historialRealBD = new ArrayList<>();
        historialRealBD.add(new Venta("M-777", "P-777", 4, LocalDateTime.now().minusDays(3)));
        historialRealBD.add(new Venta("M-777", "P-777", 6, LocalDateTime.now().minusDays(1)));
        
        // El componente de datos se integra devolviendo la colección acumulada
        when(ventaDAO.buscarDesdeFecha(eq("M-777"), eq("P-777"), any(LocalDateTime.class)))
            .thenReturn(historialRealBD);

        LocalDate fechaCalculada = servicio.estimarFechaReposicion(maquina, producto);

        // Total vendido = 10 unidades en 5 días -> Velocidad = 2 uds/día.
        // Días de vida restante = 40 unidades actuales / 2 = 20 días.
        // Fecha propuesta = Hoy + 20 días - 1 día de margen de seguridad = Hoy + 19 días.
        LocalDate fechaEsperada = LocalDate.now().plusDays(19);
        
        assertEquals(fechaEsperada, fechaCalculada, 
                     "Fallo de integración: El sumatorio de unidades del historial disperso no es correcto.");
        
        // Verificamos la interacción con el componente persistente
        verify(ventaDAO, times(1)).buscarDesdeFecha(anyString(), anyString(), any(LocalDateTime.class));
    }

    @Test
    @Tag("Integracion")
    @DisplayName("CP-67: Aislamiento del Ciclo Temporal (Verificación de la fecha de corte)")
    void testIntegracion_FiltroMargenTemporal() {
        stock.setFechaUltimaReposicion(LocalDate.now().minusDays(2));
        maquina.addStock(stock);

        List<Venta> historialFiltrado = new ArrayList<>();
        historialFiltrado.add(new Venta("M-777", "P-777", 10, LocalDateTime.now().minusDays(1)));

        // Determinamos la fecha de corte exacta generada por la entidad Stock
        LocalDateTime fechaCorteEsperada = stock.getFechaUltimaReposicion().atStartOfDay();
        
        // Verificamos si el servicio sabe enviar la fecha exacta de corte requerida al DAO
        when(ventaDAO.buscarDesdeFecha("M-777", "P-777", fechaCorteEsperada))
            .thenReturn(historialFiltrado);

        LocalDate fechaCalculada = servicio.estimarFechaReposicion(maquina, producto);

        // Ventas del ciclo = 10 unidades en 2 días -> Velocidad = 5 uds/día.
        // Días de vida = 40 actuales / 5 = 8 días.
        // Proyección final = Hoy + 8 días - 1 día de margen = Hoy + 7 días.
        LocalDate fechaEsperada = LocalDate.now().plusDays(7);
        
        assertEquals(fechaEsperada, fechaCalculada, 
                     "Fallo de integración: El servicio no filtra las capas mediante la fecha de corte exacta.");
    }

    @Test
    @Tag("Integracion")
    @DisplayName("CP-68: Muelle Vacío sin Transacciones (Excepción integrada)")
    void testIntegracion_MuelleVacioSinVentas() {
        stock.setFechaUltimaReposicion(LocalDate.now().minusDays(4));
        maquina.addStock(stock);

        // La base de datos integrada devuelve una lista vacía de registros para este ciclo de reposición
        when(ventaDAO.buscarDesdeFecha(anyString(), anyString(), any(LocalDateTime.class)))
            .thenReturn(new ArrayList<Venta>());

        //(Control de excepciones lógicas)
        ArithmeticException excepcion = assertThrows(ArithmeticException.class, () -> {
            servicio.estimarFechaReposicion(maquina, producto);
        }, "Se esperaba un error controlado al integrar un flujo sin datos de ventas.");
        
        assertEquals("Sin datos de rotación.", excepcion.getMessage());
    }
}