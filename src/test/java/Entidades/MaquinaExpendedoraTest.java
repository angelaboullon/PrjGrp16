package Entidades;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import Entidades.MaquinaExpendedora;
import Excepciones.InvalidIdentifierException;

class MaquinaExpendedoraTest {

    private MaquinaExpendedora maquina;

    @BeforeEach
    void setUp() {
        // Se inicializa una nueva máquina antes de CADA test (sea de la HU1 o futuras)
        maquina = new MaquinaExpendedora();
    }

    // ==========================================
    // PRUEBAS DE LA HU1 (Alta de Máquina)
    // ==========================================
    @Nested
    @Tag("HU1")
    @DisplayName("HU1: Validaciones de Identificador")
    class PruebasHU1 {

        @ParameterizedTest
        @ValueSource(strings = {"M-001", "M-500", "M-999"})
        @DisplayName("CP-01: Formato Válido (M + 3 dígitos)")
        void testSetId_Valido(String idValido) { 
            assertDoesNotThrow(() -> maquina.setID(idValido));
            assertEquals(idValido, maquina.getId());
        }

        @ParameterizedTest
        @ValueSource(strings = {"A-123", "M-12", "M-ABCD", ""})
        @DisplayName("CP-02: Formato o longitud inválida")
        void testSetId_InvalidoFormato(String idInvalido) { 
            assertThrows(InvalidIdentifierException.class, () -> maquina.setID(idInvalido));
        }

        @Test
        @DisplayName("CP-03: ID Nulo")
        void testSetId_Nulo() { 
            assertThrows(InvalidIdentifierException.class, () -> maquina.setID(null));
        }
        
        @Test
        @DisplayName("CP-04: Asignar Localización correctamente")
        void testSetLocalizacion() { 
            Localizacion locTest = new Localizacion();
            maquina.setLocalizacion(locTest);
            
            // Verificamos que se ha guardado bien usando el getter
            assertEquals(locTest, maquina.getLocalizacion());
        }
        
        @Test
        @Tag("CajaNegra")
        @DisplayName("CP-05: Cadena vacía o espacios en ID (EP)")
        void testIdCadenaVacia() {
            String idVacio = "   ";

            // Act & Assert
            // Usamos assertThrows evaluando la excepción y el mensaje
            assertThrows(InvalidIdentifierException.class, () -> {
                maquina.setID(idVacio);
            }, "Debería lanzar InvalidIdentifierException al pasar una cadena vacía o compuesta solo por espacios");
        }
        
        @Test
        @Tag("CajaNegra")
        @DisplayName("CP-06: Formato incompleto en ID (BVA)")
        void testIdFormatoIncompleto() {
            String idIncompleto = "M-1"; // Faltan dígitos según el patrón M-XXX

            // Act & Assert
            assertThrows(InvalidIdentifierException.class, () -> {
                maquina.setID(idIncompleto);
            }, "Debería lanzar InvalidIdentifierException al pasar un ID que no completa los 3 dígitos requeridos");
        }
        
        @Test
        @Tag("CajaNegra")
        @Tag("Robustez")
        @DisplayName("CP-07: Protección contra Localización nula")
        void testSetLocalizacionNula() {
            assertThrows(IllegalArgumentException.class, () -> {
                maquina.setLocalizacion(null);
            }, "Debería lanzar IllegalArgumentException al intentar asignar una localización nula");
        }
    }
}