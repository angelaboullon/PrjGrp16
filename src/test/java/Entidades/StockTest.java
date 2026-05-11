package Entidades;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import Excepciones.FullCapacityException;

@Tag("CajaNegra")
@Tag("HU2")
class StockTest {

    @Test
    @DisplayName("CP-13, 14: Cantidad actual no negativa")
    void testStockNegativo() {
        Stock s = new Stock();
        assertAll("Validación stock",
            () -> assertDoesNotThrow(() -> s.setCantidadActual(0)),
            () -> assertThrows(IllegalArgumentException.class, () -> s.setCantidadActual(-1))
        );
    }

    @Test
    @DisplayName("CP-15: Desbordamiento de muelle")
    void testIncrementarExceso() {
        Stock s = new Stock();
        s.setCapacidadMax(10);
        s.setCantidadActual(10);
        // assertThrows gestiona la excepción FullCapacityException internamente
        assertThrows(FullCapacityException.class, () -> s.incrementar(1));
    }
    

    @Test
    @DisplayName("CP-27: Incremento de existencias exitoso")
    void testIncrementarExito() throws FullCapacityException {
        // Arrange 
        Stock s = new Stock();
        s.setCapacidadMax(10);
        s.setCantidadActual(5);
        
        // Act 
        s.incrementar(2); // 5 + 2 = 7 (menor que 10)
        
        // Assert 
        assertEquals(7, s.getCantidadActual(), "La cantidad debe actualizarse correctamente tras el incremento");
        assertNotNull(s.getFechaUltimaReposicion(), "Debe registrarse la fecha de la operación");
    }
}