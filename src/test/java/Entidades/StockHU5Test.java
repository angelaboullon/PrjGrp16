package Entidades;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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

    @ParameterizedTest(name = "cantidad={0}, umbral={1} → esperado={2}")
    @DisplayName("CP-48, 49, 50: Validación de isBajoMinimos (AVL)")
    @CsvSource({
        "4,  5, true",    // CP-48: Límite inferior — bajo mínimos (Alerta)
        "5,  5, false",   // CP-49: Frontera exacta — igual al umbral (No alerta)
        "10, 5, false"    // CP-50: Límite superior — por encima del umbral (Seguro)
    })
    void testIsBajoMinimos(int cantidad, int umbral, boolean esperado) {
        stock.setCantidadActual(cantidad);
        assertEquals(esperado, stock.isBajoMinimos(umbral),
            "Con cantidad=" + cantidad + " y umbral=" + umbral + " isBajoMinimos debe ser " + esperado);
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
