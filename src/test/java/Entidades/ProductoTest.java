package Entidades;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import Excepciones.*;

@Tag("CajaNegra")
@Tag("HU2")
@DisplayName("Pruebas de Unidad: Entidad Producto")
class ProductoTest {

    private Producto p;

    @BeforeEach
    void setUp() { p = new Producto(); } 

    @Nested
    @DisplayName("CP-01 al 03: Validación de ID (P-XXX)")
    class IdTests {
        @Test
        @DisplayName("ID válido")
        void testIdValido() { 
            assertDoesNotThrow(() -> p.setId("P-001")); 
        }

        @ParameterizedTest
        @ValueSource(strings = {"A-123", "P-12", "P-1234", ""})
        @DisplayName("IDs inválidos")
        void testIdsInvalidos(String id) {
            assertThrows(InvalidIdentifierException.class, () -> p.setId(id)); 
        }
    }

    @Nested
    @DisplayName("CP-04 al 07: Límites del Nombre (3-50 car.)")
    class NombreTests {
        @Test
        @DisplayName("Límites exactos (3 y 50)")
        void testLimitesNombre() {
            String nom50 = "A".repeat(50);
            assertAll("Nombres en frontera",
                () -> assertDoesNotThrow(() -> p.setNombre("abc")),
                () -> assertDoesNotThrow(() -> p.setNombre(nom50))
            ); 
        }

        @Test
        @DisplayName("Nombres fuera de rango (2 y 51)")
        void testNombreFueraRango() {
            String nom51 = "A".repeat(51);
            assertAll("Nombres inválidos",
                () -> assertThrows(MalformedNameException.class, () -> p.setNombre("ab")),
                () -> assertThrows(MalformedNameException.class, () -> p.setNombre(nom51))
            );
        }
    }

    @Nested
    @DisplayName("CP-08 al 12: Límites de Precio (0.01 - 1000.0)")
    class PrecioTests {
        @ParameterizedTest
        @ValueSource(doubles = {0.01, 1000.0})
        @DisplayName("Precios en frontera")
        void testPrecioFrontera(double precio) { 
            assertDoesNotThrow(() -> p.setPrecio(precio)); 
        }

        @ParameterizedTest
        @ValueSource(doubles = {0.0, -0.01, 1000.01})
        @DisplayName("Precios inválidos")
        void testPrecioInvalido(double precio) {
            assertThrows(InvalidPriceException.class, () -> p.setPrecio(precio));
        }
    }
    
    @Test
    @DisplayName("CP-25, 26: Validación de valores nulos")
    void testValoresNulos() {
        assertAll("Cierre de ramas nulas",
            () -> assertThrows(InvalidIdentifierException.class, () -> p.setId(null)),
            () -> assertThrows(MalformedNameException.class, () -> p.setNombre(null))
        );
    }
    
    
    
}