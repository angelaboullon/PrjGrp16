package Servicio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.*;
import org.mockito.*;

import DAO.*;
import Entidades.*;
import Excepciones.*;

@Tag("HU4-HU5")
@DisplayName("HU4 y HU5: Venta y Detección de Bajo Stock")
class VendingServiceHU4HU5Test {

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

    // ============================================================
    // HU4 - Actualizar stock tras venta
    // ============================================================
    @Nested
    @DisplayName("HU4: venderProducto")
    class HU4VenderProducto {

        @Test
        @DisplayName("HU4-01: Venta válida descuenta stock y registra en DAO")
        void testVentaExitosa() throws Exception {
            Producto p = new Producto(); p.setId("P-001");
            Stock s = new Stock(); s.setProducto(p); s.setCantidadActual(10); s.setCapacidadMax(20);
            MaquinaExpendedora m = new MaquinaExpendedora(); m.addStock(s);

            Venta venta = service.venderProducto(m, p, 3);

            assertEquals(7, s.getCantidadActual(), "El stock debe bajar en la cantidad vendida");
            assertNotNull(venta, "Debe devolver el objeto Venta creado");
            assertEquals("P-001", venta.getIdProducto());
            assertEquals(3, venta.getUnidades());
            verify(ventaDAO, times(1)).registrar(venta);
        }

        @Test
        @DisplayName("HU4-02: Excepción si el producto no está en la máquina (Condición 3)")
        void testVentaProductoNoAsignado() {
            Producto p = new Producto();
            MaquinaExpendedora m = new MaquinaExpendedora(); // máquina sin stock

            assertThrows(EntityNotFoundException.class,
                    () -> service.venderProducto(m, p, 1));
        }

        @Test
        @DisplayName("HU4-03: Excepción si la cantidad supera el stock (Condición 2 y 3)")
        void testVentaStockInsuficiente() throws Exception {
            Producto p = new Producto(); p.setId("P-001");
            Stock s = new Stock(); s.setProducto(p); s.setCantidadActual(2); s.setCapacidadMax(10);
            MaquinaExpendedora m = new MaquinaExpendedora(); m.addStock(s);

            assertThrows(InsufficientStockException.class,
                    () -> service.venderProducto(m, p, 5));
            assertEquals(2, s.getCantidadActual(), "El stock no debe modificarse si la venta falla");
        }

        @Test
        @DisplayName("HU4-04: El stock nunca queda en negativo (Condición 2)")
        void testStockNuncaNegativo() throws Exception {
            Producto p = new Producto(); p.setId("P-001");
            Stock s = new Stock(); s.setProducto(p); s.setCantidadActual(0); s.setCapacidadMax(10);
            MaquinaExpendedora m = new MaquinaExpendedora(); m.addStock(s);

            assertThrows(InsufficientStockException.class,
                    () -> service.venderProducto(m, p, 1));
            assertTrue(s.getCantidadActual() >= 0, "El stock no puede ser negativo");
        }
    }

    // ============================================================
    // HU5 - Detectar productos a reponer
    // ============================================================
    @Nested
    @DisplayName("HU5: consultarProductosBajoStock")
    class HU5ConsultarBajoStock {

        private MaquinaExpendedora construirMaquinaConStock(Producto p, int actual, int max, LocalDate fechaRepo) {
            Stock s = new Stock();
            s.setProducto(p);
            s.setCantidadActual(actual);
            s.setCapacidadMax(max);
            s.setFechaUltimaReposicion(fechaRepo);
            MaquinaExpendedora m = new MaquinaExpendedora();
            m.addStock(s);
            return m;
        }

        @Test
        @DisplayName("HU5-01: Detecta producto por debajo del umbral estático (Condición 1 y 3)")
        void testBajoUmbralEstatico() {
            Producto p = new Producto();
            MaquinaExpendedora m = construirMaquinaConStock(p, 3, 20, LocalDate.now().minusDays(5));

            when(ventaDAO.buscarDesdeFecha(any(), any(), any())).thenReturn(new ArrayList<>());

            List<Stock> resultado = service.consultarProductosBajoStock(m, 5);

            assertEquals(1, resultado.size(), "El producto con stock < umbral debe aparecer");
        }

        @Test
        @DisplayName("HU5-02: Detecta producto próximo a agotarse por velocidad de consumo (Condición 2)")
        void testProximoAgotarsePorVelocidad() throws Exception {
            Producto p = new Producto(); p.setId("P-001");
            // Stock actual: 6, se venden 4 por día → se agota en 1.5 días (≤ 3)
            MaquinaExpendedora m = construirMaquinaConStock(p, 6, 50, LocalDate.now().minusDays(5));

            List<Venta> ventas = new ArrayList<>();
            ventas.add(new Venta(null, "P-001", 20, LocalDateTime.now().minusDays(5)));
            when(ventaDAO.buscarDesdeFecha(any(), eq("P-001"), any())).thenReturn(ventas);

            List<Stock> resultado = service.consultarProductosCriticosPorTiempo(m, 3);

            assertEquals(1, resultado.size(), "Producto que se agota pronto debe detectarse");
        }

        @Test
        @DisplayName("HU5-03: No marca producto con stock suficiente y sin consumo urgente")
        void testStockSuficienteSinUrgencia() throws Exception {
            Producto p = new Producto(); p.setId("P-001");
            MaquinaExpendedora m = construirMaquinaConStock(p, 30, 50, LocalDate.now().minusDays(10));

            when(ventaDAO.buscarDesdeFecha(any(), any(), any())).thenReturn(new ArrayList<>());

            List<Stock> resultado = service.consultarProductosCriticosPorTiempo(m, 3);

            assertTrue(resultado.isEmpty(), "No debe marcar producto con stock holgado y sin ventas recientes");
        }

        @Test
        @DisplayName("HU5-04: Máquina sin productos devuelve lista vacía")
        void testMaquinaSinStock() {
            MaquinaExpendedora m = new MaquinaExpendedora();

            List<Stock> resultado = service.consultarProductosBajoStock(m, 5);

            assertTrue(resultado.isEmpty());
        }
    }
}
