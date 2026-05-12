package Servicio;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import Entidades.MaquinaExpendedora;
import Entidades.Producto;
import Entidades.Stock;

@DisplayName("Pruebas de Integración y Robustez: HU5")
class VendingServiceHU5Test {

    private VendingService servicio;
    private MaquinaExpendedora maquina;

    @BeforeEach
    void setUp() throws Exception {
        servicio = new VendingService();
        maquina = new MaquinaExpendedora();
        maquina.setID("M-001");
        maquina.setCapacidad(100);
    }

    // ==========================================
    // SUBPRUEBA 1: FILTRADO ESTÁTICO (UMBRAL)
    // ==========================================

    @Test
    @DisplayName("CP-57 y CP-58: Filtrado Bajo Stock — Valores Límite e Integración")
    void testFiltradoBajoStock_ValoresLimite() throws Exception {
        int umbral = 5;

        // Alerta (CP-57): Cantidad = 4. Debe incluirse (4 < 5).
        Stock stockInferior = new Stock();
        Producto p1 = new Producto(); p1.setId("P-001");
        stockInferior.setProducto(p1);
        stockInferior.setCantidadActual(4);

        // Frontera (CP-58): Cantidad = 5. No debe incluirse (5 < 5 es false).
        Stock stockFrontera = new Stock();
        Producto p2 = new Producto(); p2.setId("P-002");
        stockFrontera.setProducto(p2);
        stockFrontera.setCantidadActual(5);

        // Seguro: Cantidad = 10. No debe incluirse.
        Stock stockSuperior = new Stock();
        Producto p3 = new Producto(); p3.setId("P-003");
        stockSuperior.setProducto(p3);
        stockSuperior.setCantidadActual(10);

        maquina.addStock(stockInferior);
        maquina.addStock(stockFrontera);
        maquina.addStock(stockSuperior);

        List<Stock> resultado = servicio.consultarProductosBajoStock(maquina, umbral);

        assertEquals(1, resultado.size(), "Solo el stock con cantidad < umbral debe aparecer");
        assertEquals("P-001", resultado.get(0).getProducto().getId(), "Debe ser el producto P-001");
    }

    // ==========================================
    // SUBPRUEBA 1: ROBUSTEZ (ESCUDOS DE SEGURIDAD)
    // ==========================================

    @Test
    @DisplayName("CP-53: Robustez — Máquina nula lanza IllegalArgumentException")
    void testMaquinaNula_LanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.consultarProductosBajoStock(null, 5),
                "Debe lanzar IllegalArgumentException si la máquina es nula");
    }

    @Test
    @DisplayName("CP-54: Robustez — Umbral negativo lanza IllegalArgumentException")
    void testUmbralNegativo_LanzaExcepcion() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.consultarProductosBajoStock(maquina, -1),
                "Debe lanzar IllegalArgumentException si el umbral es negativo");
    }

    // ==========================================
    // SUBPRUEBA 2: FILTRADO POR TIEMPO (VELOCIDAD DE CONSUMO)
    // ==========================================

    @Test
    @DisplayName("CP-T1: Producto crítico por tiempo — se agota en ≤ diasUmbral días")
    void testProductoCriticoPorTiempo() throws Exception {
        // Arrange: stock = 30, se repuso hace 10 días.
        // Vendemos 20 unidades → stock = 10, velocidad = 20/10 = 2 unidades/día.
        // Días hasta agotamiento = 10/2 = 5 ≤ 7 → crítico.
        Producto p1 = new Producto(); p1.setId("P-001");
        Stock s1 = new Stock();
        s1.setProducto(p1);
        s1.setCantidadActual(30);
        s1.setCapacidadMax(100);
        s1.setFechaUltimaReposicion(LocalDate.now().minusDays(10));
        maquina.addStock(s1);

        servicio.venderProducto(maquina, p1, 20); // stock pasa a 10, venta registrada

        List<Stock> resultado = servicio.consultarProductosCriticosPorTiempo(maquina, 7);

        assertEquals(1, resultado.size(), "El producto debe aparecer como crítico");
        assertEquals("P-001", resultado.get(0).getProducto().getId());
    }

    @Test
    @DisplayName("CP-T2: Producto NO crítico por tiempo — días restantes > diasUmbral")
    void testProductoNoCriticoPorTiempo() throws Exception {
        // Arrange: stock = 100, se repuso hace 10 días.
        // Vendemos 5 unidades → stock = 95, velocidad = 5/10 = 0.5 unidades/día.
        // Días hasta agotamiento = 95/0.5 = 190 > 3 → no crítico.
        Producto p1 = new Producto(); p1.setId("P-001");
        Stock s1 = new Stock();
        s1.setProducto(p1);
        s1.setCantidadActual(100);
        s1.setCapacidadMax(150);
        s1.setFechaUltimaReposicion(LocalDate.now().minusDays(10));
        maquina.addStock(s1);

        servicio.venderProducto(maquina, p1, 5); // stock pasa a 95, venta registrada

        List<Stock> resultado = servicio.consultarProductosCriticosPorTiempo(maquina, 3);

        assertTrue(resultado.isEmpty(), "El producto con mucho stock no debe aparecer como crítico");
    }

    @Test
    @DisplayName("CP-T3: Producto sin historial de ventas no aparece como crítico")
    void testProductoSinVentas_NoCritico() throws Exception {
        // Un producto sin ventas registradas tiene velocidad = 0, no puede ser crítico.
        Producto p1 = new Producto(); p1.setId("P-001");
        Stock s1 = new Stock();
        s1.setProducto(p1);
        s1.setCantidadActual(5);
        s1.setCapacidadMax(50);
        s1.setFechaUltimaReposicion(LocalDate.now().minusDays(3));
        maquina.addStock(s1);

        // No se registra ninguna venta.
        List<Stock> resultado = servicio.consultarProductosCriticosPorTiempo(maquina, 3);

        assertTrue(resultado.isEmpty(), "Sin historial de ventas no hay velocidad y no debe ser crítico");
    }

    @Test
    @DisplayName("CP-T4: Robustez — Máquina nula en consultarProductosCriticosPorTiempo")
    void testCriticoPorTiempo_MaquinaNula() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.consultarProductosCriticosPorTiempo(null, 3));
    }

    @Test
    @DisplayName("CP-T5: Robustez — Días umbral negativo en consultarProductosCriticosPorTiempo")
    void testCriticoPorTiempo_DiasUmbralNegativo() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.consultarProductosCriticosPorTiempo(maquina, -1));
    }
}
