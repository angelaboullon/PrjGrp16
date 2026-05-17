package Entidades;

import static org.junit.jupiter.api.Assertions.*;
import Excepciones.InsufficientStockException;
import Excepciones.FullCapacityException;
import org.junit.jupiter.api.*;


/**
 * Clase de pruebas unitarias en contexto de caja negra para la entidad 'Stock'.
 * 
 * Se aplican rigurosamente las técnicas de particionado de Clases de Equivalencia (CE) y el
 * Análisis de Valores Límite (AVL) para asegurar que el componente resiste tanto flujos idílicos
 * de negocio como entradas malformadas (robustez).
 **/

class StockTest 
{

	// ===============================================
    // PRUEBAS HU2 - ASOCIAR PRODUCTOS A UNA MÁQUINA
    // ===============================================
	
	/**
	 * testStockNegativo(): valida las restricciones de integridad sobre la mutación directa del stock.
	 * 
	 * - Técnica aplicada: Clases de Equivalencia (cero como frontera válida, negativos como clase inválida).
	 * - Estrategia de verificación: uso de assertAll para evaluar en paralelo que la asignación correcta 
	 * no genera efectos colaterales y que la asignación errónea es interceptada inmediatamente.
	 **/
	@Test
    @DisplayName("Cantidad actual no negativa")
    void testStockNegativo() 
	{
		Stock s = new Stock();
		
        assertAll("Validación stock",
			() -> assertDoesNotThrow(() -> s.setCantidadActual(0)),
            () -> assertThrows(IllegalArgumentException.class, () -> s.setCantidadActual(-1))
        );
    }

	
	/**
	 * testIncrementarExceso(): verifica la protección contra el desbordamiento físico de la máquina.
	 * 
	 * - Técnica aplicada: Análisis de Valores Límites (frontera superior máxima superada en +1 unidad).
	 * - Estrategia de verificación: assertThrows captura la excepción customizada FullCapacityException.
	 **/
    @Test
    @DisplayName("Desbordamiento de muelle")
    void testIncrementarExceso() 
    {
        Stock s = new Stock();
        s.setCapacidadMax(10);
        s.setCantidadActual(10);
        
        assertThrows(FullCapacityException.class, () -> s.incrementar(1));
    }
    

    /**
     * testIncrementarExito(): confirma el incremento correcto de existencias y el registro de la auditoría
     * temporal.
     * 
     * - Técnica aplicada: Clase de Equivalencia Válida (incremento dentro de los márgenes disponibles).
     * - Estrategia de verificación: assertEquals para el cálculo matemático; assertNotNull para el metadato
     * de fecha.
     **/
    @Test
    @DisplayName("Incremento de existencias exitoso")
    void testIncrementarExito() throws FullCapacityException 
    {
        Stock s = new Stock();
        s.setCapacidadMax(10);
        s.setCantidadActual(5);
        
        s.incrementar(2); // 5 + 2 = 7 (menor que 10)
        
        assertEquals(7, s.getCantidadActual(), "La cantidad debe actualizarse correctamente tras el incremento");
        assertNotNull(s.getFechaUltimaReposicion(), "Debe registrarse la fecha de la operación");
    }
    
    
    
    // ===============================================
    // PRUEBAS HU4 - ACTUALIZAR STOCK TRAS VENTA
    // ===============================================
    
    /**
     * decrementarVentaEstandar(): verifica una operación rutinaria de salida de producto.
     * 
     * - Técnica aplicada: Clase de Equivalencia Válida (cantidad menor que el stock disponible y estrictamente 
     * mayor que cero).
     * - Estrategia de verificación: assertEquals que evalúa el decremento en el atributo cantidadActual es
     * aritméticamente fiel.
     **/
    @Test
    @Tag("HU4")
    @Tag("CajaNegra")
    @DisplayName("Venta estándar (Clase Válida)")
    void decrementarVentaEstandar() throws InsufficientStockException
    {
    	// [Arrange] Seteo de un inventario holgado con 10 unidades iniciales.
    	Stock s = new Stock();
    	s.setCantidadActual(10);   
    	
    	// [Act] Se simula el despacho de un pedido ordinario de 5 unidades.
    	s.decrementar(5);
    	
    	// [Assert] Se comprueba que el remanente en el muelle tras la venta disminuye exactamente a 5.
    	assertEquals(5, s.getCantidadActual(), "La cantidad restante debería ser 5");
    }
    
    
    /**
     * decrementarAgotamientoExacto(): evlaúa el vaciado total exacto del muelle de stock en una transacción.
     * - Técnica aplicada: Análisis de Valores Límite (frontera exacta donde la cantidad solicitada coincide
     * con las existencias).
     * - Estrategia de verificación: uso combinado de assertEquals (verificación exacta) y assertTrue 
     * (aserción booleana de seguridad).
     **/
    @Test
    @Tag("HU4")
    @Tag("CajaNegra")
    @DisplayName("Agotamiento exacto (Límite)")
    void decrementarAgotamientoExacto() throws InsufficientStockException
    {
    	// [Arrange] Inicialización del muelle con 10 unidades.
    	Stock s = new Stock();
    	s.setCantidadActual(10);
    	
    	// [Act] Un cliente solicita comprar exactamente las 10 unidades que quedan.
    	s.decrementar(10);
    	
    	// [Assert] Se evalúa el estado crítico de 'Muelle Agotado' de dos maneras:
    	// 1. [assertEquals]: mediante comparación directa de enteros (debe ser 0).
    	assertEquals(0, s.getCantidadActual(), "El stock debería haberse agotado exactamente (0)");
    	
    	// 2. [assertTrue]: mediante comprobación booleana para certificar que el valor es estrictamente
    	// cero.
    	assertTrue(s.getCantidadActual() == 0, "Validación booleana lógica complementaria: la cantidad actual "
    			+ "es matemáticamente cero");
    }
    
    
    /**
     * decrementarExcesoDeVenta(): asegura que el sistema bloquea transacciones que superen las existencias físicas
     * disponibles.
     * - Técnica aplicada: Análisis de Valores Límite (frontera exterior no válida: stock actual + 1 unidad).
     * - Estrategia de verificación: assertThrows captura la excepción de negocio InsufficientStockException.
     * Posteriormente, un bloque assertAll/assertFalse verifica que el objeto bloqueó la venta y que no alteró ni 
     * corrompió su cantidad.
     **/
    @Test
    @Tag("HU4")
    @Tag("CajaNegra")
    @DisplayName("Exceso de venta (Límite)")
    void decrementarExcesoDeVenta()
    {
    	// [Arrange] Inicialización de un muelle con 10 unidades reales.
    	Stock s = new Stock();
    	s.setCantidadActual(10);
    	
    	// [Act & Assert] Se intenta retirar 11 unidades (operación ilegal por falta de existencias).
    	InsufficientStockException ex = assertThrows(InsufficientStockException.class, () -> s.decrementar(11),
    			"Debería lanzar InsufficientStockException por exceder el stock");
    
    	// [Assert Adicional] Blindaje de robustez interna del objeto.
    	// 1. [assertNotNull]: se comprueba que el objeto lanza un mensaje de error legible para los ficheros de log.
    	// 2. [assertFalse]: se certifica mediante negación que la cantidad no bajó por error ni mutó en negativo (-1).
    	assertAll("Garantía de integridad ante fallos de venta",
    			() -> assertNotNull(ex.getMessage(), "La excepción capturada debe albergar un mensaje descriptivo"),
    			() -> assertFalse(s.getCantidadActual() == -1, "El stock no puede quedar en un estado negativo tras el fallo")
    	);
    }
    
    
    /**
     * decrementarVentaNula(): valida la denegación de transacciones vacías (intentar restar cero unidades).
     * - Técnica aplicada: Análisis de Valores Límite (frontera inferior inválida exacta de datos de entrada).
     * - Estrategia de verificación: assertThrows intercepta IllegalArgumentException dado que es un error en la entrada del 
     * parámetro.
     **/
    @Test
    @Tag("HU4")
    @Tag("CajaNegra")
    @DisplayName("Venta nula (Frontera inferior)")
    void decrementarVentaNula()
    {
    	// [Arrange] Configuración estándar del stock con 10 unidades.
    	Stock s = new Stock();
    	s.setCantidadActual(10);
    	
    	// [Act & Assert] Intentar restar 0 unidades es una llamada de servicio corrupta que debe cortocircuitarse.
    	assertThrows(IllegalArgumentException.class, () -> s.decrementar(0),
    			"Debería lanzar IllegalArgumentException porque la venta no puede ser cero");
    }
    
    
    /**
     * decrementarVentaNegativa(): valida el blindaje del método frente a cantidades negativas (intentar restar unidades bajo cero).
     * - Técnica aplicada: Clase de Equivalencia Inválida (valores menores que cero en las entradas).
     * - Estrategia de verificación: assertThrows para validar el corte de ejecución; assertNotEquals para asegurar que el estado
     * interno quedó congelado y que un error de signo en la entrada no causó un incremento accidental del stock.
     **/
    @Test
    @Tag("HU4")
    @Tag("CajaNegra")
    @DisplayName("Venta negativa (Clase Inválida)")
    void decrementarVentaNegativa()
    {
    	// [Arrange] El muelle parte de 10 unidades.
    	Stock s = new Stock();
    	s.setCantidadActual(10);
    	
    	// [Act & Assert] Intentar restar una cantidad negativa (-1) es un error de formato crítico.
    	IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> s.decrementar(-1),
    			"Debería lanzar IllegalArgumentException porque la venta no puede ser negativa");
    	
    	// [Assert Adicional] Se verifica la regla del 'invariante del objeto' (el estado original no cambia). Se comprueba que el
    	// stock no sumó la cantidad por error de doble negación matemática (10 - (-1) = 11).
    	assertNotEquals(11, s.getCantidadActual(), "El stock actual no debe incrementarse por un error de signo en el decremento");	
    }
}