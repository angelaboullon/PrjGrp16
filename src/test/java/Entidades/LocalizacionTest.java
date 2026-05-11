package Entidades;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import Entidades.Localizacion;
import Excepciones.CoordinateOutOfRangeException;

class LocalizacionTest {

    private Localizacion loc;

    @BeforeEach
    void setUp() {
        loc = new Localizacion();
    }

    // ==========================================
    // PRUEBAS DE LA HU1 (Rangos Geográficos)
    // ==========================================
    @Nested
    @Tag("HU1")
    @DisplayName("HU1: Validaciones de Límites Geográficos")
    class PruebasHU1 {

        // --- Pruebas de Latitud ---
        @ParameterizedTest
        @ValueSource(doubles = {-90.0, 0.0, 90.0})
        @DisplayName("CP-08: Latitud dentro de los límites válidos")
        void testSetLatitud_LimitesValidos(double lat) { 
            assertDoesNotThrow(() -> loc.setLatitud(lat));
            assertEquals(lat, loc.getLatitud());
        }

        @ParameterizedTest
        @ValueSource(doubles = {-90.1, 90.1, 150.0})
        @DisplayName("CP-09: Latitud fuera de límites")
        void testSetLatitud_LimitesInvalidos(double lat) { 
            assertThrows(CoordinateOutOfRangeException.class, () -> loc.setLatitud(lat));
        }

        // --- Pruebas de Longitud ---
        @ParameterizedTest
        @ValueSource(doubles = {-180.0, 0.0, 180.0})
        @DisplayName("CP-10: Longitud dentro de los límites válidos")
        void testSetLongitud_LimitesValidos(double lon) { 
            assertDoesNotThrow(() -> loc.setLongitud(lon));
            assertEquals(lon, loc.getLongitud());
        }

        @ParameterizedTest
        @ValueSource(doubles = {-180.1, 180.1, 200.0})
        @DisplayName("CP-11: Longitud fuera de límites")
        void testSetLongitud_LimitesInvalidos(double lon) { 
            assertThrows(CoordinateOutOfRangeException.class, () -> loc.setLongitud(lon));
        }
        
        @Test
        @Tag("CajaNegra")
        @DisplayName("CP-12: Límite exacto permitido en latitud (BVA)")
        void testLatitudLimiteExacto() throws Exception {
            // Arrange
            Localizacion loc = new Localizacion();
            double latitudLimite = 90.0; // Valor justo en la frontera permitida

            // Act
            loc.setLatitud(latitudLimite);

            // Assert
            assertEquals(latitudLimite, loc.getLatitud(), 
                "La latitud debe aceptar el límite exacto de 90.0 sin lanzar ninguna excepción");
        }
    }
}
