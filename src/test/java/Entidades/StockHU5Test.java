package Entidades;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Pruebas de Unidad: Entidad Stock (HU5)")
class StockHU5Test {

    private Stock stock;

    @BeforeEach
    void setUp() {
        stock = new Stock();
    }

    // ==========================================
    // CP-48 a CP-50: LÍMITES DE UMBRAL (ESTÁTICO)
    // ==========================================

    @Test
    @DisplayName("CP-48, 49, 50: Validación de isBajoMinimos (AVL)")
    void testIsBajoMinimos() {
        int umbral = 5;

        // CP-48: Cantidad menor (Alerta)
        stock.setCantidadActual(4);
        assertTrue(stock.isBajoMinimos(umbral), "CP-48: Con 4 unidades y umbral 5, debe retornar true.");

        // CP-49: Cantidad igual (Frontera exacta)
        stock.setCantidadActual(5);
        assertFalse(stock.isBajoMinimos(umbral), "CP-49: Con 5 unidades y umbral 5, debe retornar false.");

        // CP-50: Cantidad mayor (Seguro)
        stock.setCantidadActual(10);
        assertFalse(stock.isBajoMinimos(umbral), "CP-50: Con 10 unidades y umbral 5, debe retornar false.");
    }

    // ==========================================
    // CP-51 a CP-52: PROYECCIÓN DE TIEMPO (DINÁMICO)
    // ==========================================

    @Test
    @DisplayName("CP-51: Cálculo de proyección normal de días")
    void testGetDiasParaAgotar_Normal() {
        // Arrange
        stock.setCantidadActual(10);
        stock.setVelocidadConsumo(5.0); 

        // Act & Assert (10 / 5 = 2)
        assertEquals(2, stock.getDiasParaAgotar(), "CP-51: Debe calcular exactamente 2 días restantes.");
    }

    @Test
    @DisplayName("CP-52: Límite matemático (Producto estancado / División por cero)")
    void testGetDiasParaAgotar_VelocidadCero() {
        // Arrange
        stock.setCantidadActual(10);
        stock.setVelocidadConsumo(0.0); // Velocidad 0

        // Act & Assert (Debe devolver el valor seguro 999)
        assertEquals(999, stock.getDiasParaAgotar(), "CP-52: Debe retornar 999 para evitar división por cero.");
    }
}
