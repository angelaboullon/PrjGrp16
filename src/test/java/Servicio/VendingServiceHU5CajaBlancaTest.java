package Servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import Entidades.MaquinaExpendedora;
import Entidades.Producto;
import Entidades.Stock;

@DisplayName("Pruebas de Caja Blanca: HU5 - consultarProductosBajoStock")
public class VendingServiceHU5CajaBlancaTest {

    private VendingService servicio;
    private MaquinaExpendedora maquina;

    @BeforeEach
    void setUp() throws Exception {
        servicio = new VendingService();
        maquina = new MaquinaExpendedora();
        maquina.setID("M-001");
        maquina.setCapacidad(100);
    }

    @Test
    @Tag("CajaBlanca")
    @DisplayName("C1: Máquina nula (m = null)")
    void testC1_MaquinaNula() {
        assertThrows(IllegalArgumentException.class, () -> {
            servicio.consultarProductosBajoStock(null, 5);
        }, "Debe lanzar IllegalArgumentException si la máquina es nula.");
    }

    @Test
    @Tag("CajaBlanca")
    @DisplayName("C2: Umbral negativo (umbral < 0)")
    void testC2_UmbralNegativo() {
        assertThrows(IllegalArgumentException.class, () -> {
            servicio.consultarProductosBajoStock(maquina, -1);
        }, "Debe lanzar IllegalArgumentException si el umbral es negativo.");
    }

    @Test
    @Tag("CajaBlanca")
    @DisplayName("C3: Máquina válida, lista de stock vacía")
    void testC3_ListaStockVacia() {
        List<Stock> resultado = servicio.consultarProductosBajoStock(maquina, 5);
        assertTrue(resultado.isEmpty(), "Debe retornar una lista vacía si la máquina no tiene productos.");
    }

    @Test
    @Tag("CajaBlanca")
    @DisplayName("C4: Productos en stock, ninguno cumple isBajoMinimos")
    void testC4_NingunProductoBajoMinimos() throws Exception {
        // Arrange
        int umbral = 5;
        
        Stock stock1 = new Stock();
        Producto p1 = new Producto(); p1.setId("P-001");
        stock1.setProducto(p1);
        stock1.setCantidadActual(10); // 10 >= 5 (No bajo mínimos)
        
        Stock stock2 = new Stock();
        Producto p2 = new Producto(); p2.setId("P-002");
        stock2.setProducto(p2);
        stock2.setCantidadActual(7); // 7 >= 5 (No bajo mínimos)
        
        maquina.addStock(stock1);
        maquina.addStock(stock2);

        // Act
        List<Stock> resultado = servicio.consultarProductosBajoStock(maquina, umbral);

        // Assert
        assertTrue(resultado.isEmpty(), "Debe retornar una lista vacía si ningún producto cumple la condición.");
    }

    @Test
    @Tag("CajaBlanca")
    @DisplayName("C5: Productos en stock, al menos uno cumple isBajoMinimos")
    void testC5_AlgunProductoBajoMinimos() throws Exception {
        // Arrange
        int umbral = 5;
        
        Stock stockSeguro = new Stock();
        Producto p1 = new Producto(); p1.setId("P-001");
        stockSeguro.setProducto(p1);
        stockSeguro.setCantidadActual(10); // 10 >= 5 (No bajo mínimos)
        
        Stock stockCritico = new Stock();
        Producto p2 = new Producto(); p2.setId("P-002");
        stockCritico.setProducto(p2);
        stockCritico.setCantidadActual(2); // 2 < 5 (Bajo mínimos)
        
        maquina.addStock(stockSeguro);
        maquina.addStock(stockCritico);

        // Act
        List<Stock> resultado = servicio.consultarProductosBajoStock(maquina, umbral);

        // Assert
        assertEquals(1, resultado.size(), "La lista debe contener exactamente 1 producto.");
        assertEquals("P-002", resultado.get(0).getProducto().getId(), "El producto retornado debe ser el que está bajo mínimos.");
    }
}
