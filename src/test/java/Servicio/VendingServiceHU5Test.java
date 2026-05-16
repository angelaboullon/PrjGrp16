package Servicio;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
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
    @DisplayName("CP-57 y 58: Filtrado Bajo Stock (Valores Límite e Integración)")    
    void testFiltradoBajoStock_ValoresLimite() throws Exception {        
        int umbral = 5;

        // Alerta: Cantidad = 4. Debe incluirse.        
        Stock stockInferior = new Stock();        
        Producto p1 = new Producto(); p1.setId("P-001");        
        stockInferior.setProducto(p1);        
        stockInferior.setCantidadActual(4); 

        // Frontera: Cantidad = 5. No debe incluirse.        
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

        // Act        
        List<Stock> result = servicio.consultarProductosBajoStock(maquina, umbral);

        // Assert        
        assertNotNull(result, "La lista devuelta no debe ser nula.");        
        assertEquals(1, result.size(), "Solo debería haber 1 producto por debajo del umbral.");        
        assertEquals("P-001", result.get(0).getProducto().getId(), "Fallo corregido: El ID debe coincidir exactamente.");    
    }

    @Test    
    @DisplayName("CP-56: Subprueba Lista Vacía")    
    void testFiltradoBajoStock_ListaVacia() {        
        // Act        
        List<Stock> result = servicio.consultarProductosBajoStock(maquina, 5);

        // Assert        
        assertNotNull(result, "El método debe devolver una lista vacía, no null.");        
        assertTrue(result.isEmpty(), "La lista debe estar vacía cuando la máquina no tiene productos.");    
    }

    // ==========================================
    // SUBPRUEBA 2: FILTRADO DINÁMICO (TIEMPO)
    // ==========================================

    @Test
    @DisplayName("CP-55 y 56: Filtrado por Velocidad de Consumo")
    void testFiltradoPorTiempo() throws Exception {
        int diasMargen = 3;

        // Producto que se agota rápido (Vida: 2 días) -> DEBE incluirse
        Stock sRapido = new Stock();
        sRapido.setCantidadActual(10);
        sRapido.setVelocidadConsumo(5.0); // 10 / 5 = 2 días

        // Producto estable (Vida: 10 días) -> NO debe incluirse
        Stock sLento = new Stock();
        sLento.setCantidadActual(20);
        sLento.setVelocidadConsumo(2.0); // 20 / 2 = 10 días

        maquina.addStock(sRapido);
        maquina.addStock(sLento);

        // Act
        List<Stock> result = servicio.consultarProductosCriticosPorTiempo(maquina, diasMargen);

        // Assert
        assertEquals(1, result.size(), "Solo debe detectar el producto que se agota en 2 días.");
        assertTrue(result.contains(sRapido), "El producto de alta rotación debe estar en la alerta.");
    }

    // ==========================================
    // PRUEBAS DE ROBUSTEZ 
    // ==========================================

    @Test
    @DisplayName("CP-53 y 54: Control de Robustez (Nulos y Negativos)")
    void testRobustezServicio() {
        assertAll("Comprobación de escudos de seguridad",
            () -> assertThrows(IllegalArgumentException.class, 
                () -> servicio.consultarProductosBajoStock(null, 5), "Máquina nula"),
            
            () -> assertThrows(IllegalArgumentException.class, 
                () -> servicio.consultarProductosBajoStock(maquina, -1), "Umbral negativo"),
                
            () -> assertThrows(IllegalArgumentException.class, 
                () -> servicio.consultarProductosCriticosPorTiempo(maquina, -2), "Margen negativo")
        );
    }
}
