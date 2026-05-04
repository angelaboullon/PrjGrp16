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
    // 📦 PRUEBAS DE LA HU1 (Alta de Máquina)
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
        @DisplayName("CP-02, CP-03: Formato o longitud inválida")
        void testSetId_InvalidoFormato(String idInvalido) { 
            assertThrows(InvalidIdentifierException.class, () -> maquina.setID(idInvalido));
        }

        @Test
        @DisplayName("CP-04: ID Nulo")
        void testSetId_Nulo() { 
            assertThrows(InvalidIdentifierException.class, () -> maquina.setID(null));
        }
        
        @Test
        @DisplayName("CP-04b: Asignar Localización correctamente")
        void testSetLocalizacion() { 
            Localizacion locTest = new Localizacion();
            maquina.setLocalizacion(locTest);
            
            // Verificamos que se ha guardado bien usando el getter
            assertEquals(locTest, maquina.getLocalizacion());
        }
    }
}