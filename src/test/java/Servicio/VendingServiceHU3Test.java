package Servicio;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import Entidades.MaquinaExpendedora;
import Entidades.Stock;
import DAO.MaquinaDAO;

import Excepciones.InvalidIdentifierException;
import Excepciones.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 * Clase de pruebas de integración en contexto de caja negra para la HU3.
 * 
 * Valida la cadena de consulta, recuperación y transformación de datos del inventario.
 * Se aplican de forma estricta las técnicas de Partición de Clases de Equivalencia (CE) y el Análisis de
 * Valores Límite (AVL) sobre los parámetros de entrada y colecciones de salida. 
 * Todas las dependencias físicas (DAOs) se aíslan mediante dobles de prueba controlados (Mocks).
 **/
@ExtendWith(MockitoExtension.class)
@Tag("HU3")
public class VendingServiceHU3Test 
{
	// DECLARACIÓN DE DEPENDENCIAS SIMULADAS MEDIANTE MOCKITO
	@Mock
	private MaquinaDAO maquinaDAO;
	
	
	// INYECCIÓN AUTOMATIZADA DEL SERVICIO BAJO ANÁLISIS.
	@InjectMocks
	private VendingService vendingService;

	
	// =======
	// TESTS
	// =======
	
	/**
	 * consultarStock_IdValidoConVariosProductos(): garantiza la recuperación íntegra y precisa de una colección
	 * de stock con múltiples productos.
	 * - Técnica aplicada: Clase de Equivalencia Válida (ID existente con formato M-XXX y registros asociados).
	 * - Estrategia de verificación: uso de assertNotNull para verificar la existencia de la respuesta y 
	 * assertIterableEquals para certificar que la lista devuelve mantiene un orden y contenido 'deeply equal'.
	 **/
	@Test
	@Tag("CajaNegra")
	@DisplayName("ID válido existente con varios productos en stock")
	void consultarStock_IdValidoConVariosProductos() throws Exception
	{
		// [Arrange] Configuración de los datos del escenario.
		String idMaq = "M-001";
		MaquinaExpendedora maquinaSimulada = mock(MaquinaExpendedora.class);
		
		// Se crea una lista simulada poblada con dos elementos de stock.
		List<Stock> listaStockSimulada = new ArrayList<>();
		listaStockSimulada.add(mock(Stock.class));
		listaStockSimulada.add(mock(Stock.class));
		
		// Se programan los mocks para responder con las estructuras de datos preparadas.
		when(maquinaDAO.buscarPorId(idMaq)).thenReturn(maquinaSimulada);
		when(maquinaSimulada.getListaStock()).thenReturn(listaStockSimulada);
		
		// [Act] Se invoca el flujo de recuperación de inventario de la HU3.
		List<Stock> resultadoReal = vendingService.consultarStock(idMaq);
		
		// [Assert] Validación exhaustiva.
		assertNotNull(resultadoReal, "El servicio debe retornar un puntero nulo ante consultas exitosas");
		
		// Se utiliza assertIterableEquals para validar que las colecciones son idénticas en contenido y orden.
		assertIterableEquals(listaStockSimulada, resultadoReal, "La lista de stocks devuelta debe ser profundamente equivalente"
				+ "a la almacenada en la entidad");
		
		// Verificación complementaria de tamaño de colección.
		assertEquals(2, resultadoReal.size(), "El tamaño de la lista recuperada debe coincidir con las existencias (2)");
	}
	
	
	/**
	 * consultarStock_IdInexistente(): valida la interceptación segura de identificadores no registrados e interrupción del flujo.
	 * - Técnica aplicada: Clase de Equivalencia Inválida por existencia.
	 * - Estrategia de verificación: assertThrows captura la excepción de negocio EntityNotFoundException.
	 **/
	@Test
	@Tag("CajaNegra")
	@DisplayName("ID con formato correcto pero no registrado en el sistema")
	void consultarStock_IdInexistente()
	{
		// [Arrange] ID sintácticamente correcto pero ficticio.
		String idInexistente = "M-999";
		
		// El DAO simula un fallo de localización retornando nulo tras el recorrido.
		when(maquinaDAO.buscarPorId(idInexistente)).thenReturn(null);
		
		// [Act & Assert] Intentar recuperar stock debe lanzar la excepción de negocio controlada.
		assertThrows(EntityNotFoundException.class, () -> {
			vendingService.consultarStock(idInexistente);
		}, "Se esperaba un EntityNotFoundException al consultar una máquina que no consta en el sistema");
	}
	
	
	/**
	 * consultarStock_IdFormatoIncorrecto(): valida el blinsaje frente a identificadores malformados que violen el patrón exigido
	 * (M-XXX).
	 * - Técnica aplicada: Clase de Equivalencia Inválida por formato.
	 * - Estrategia de verificación: assertThrows intercepta la excepción customizada de reobustez InvalidIdentifierException.
	 **/
	@Test
	@Tag("CajaNegra")
	@DisplayName("ID con formato incorrecto (violación de patrón alfanumérico")
	void consultarStock_IdFormatoIncorrecto()
	{
		// [Arrange] Cadena que incumple el patrón M-\d{3}
		String idMalformado = "MAQ-01";
		
		// [Act & Assert] El servicio o la entidad debe lanzar de inmediato la excepción de validación de formato.
		assertThrows(InvalidIdentifierException.class, () -> {
			vendingService.consultarStock(idMalformado);
		}, "El sistema debe rechazar identificadores malformados lanzando InvalidIdentifierException");
	}
	
	
	/**
	 * consultarStock_IdNulo(): previene fallos catastróficos por desreferenciación (NullPointerException) ante parámetros vacíos.
	 * - Técnica aplicada: Clase de Equivalencia Inválida / Robustez Límite (entrada estructuralmente nula).
	 * - Estrategia de verificación: assertThrows captura InvalidIdentifierException y assertAll evalúa que no se alteraron ni se 
	 * consultaron las capas inferiores del backend de persistencia.
	 **/
	@Test
	@Tag("CajaNegra")
	@DisplayName("Identificador con valor nulo")
	void consultarStock_IdNulo()
	{
		// [Arrange] Parámetro nulo absoluto.
		String idNulo = null;
		
		// [Act & Assert] Captura controlada del fallo de robustez de entrada.
		InvalidIdentifierException ex = assertThrows(InvalidIdentifierException.class, () -> {
			vendingService.consultarStock(idNulo);
		}, "Pasar un parámetro nulo debe forzar la interrupción con InvalidIdentifierException");
		
		// Bloqueo preventivo: se verifica de forma colectiva que el DAO jamás llegó a procesar la llamada.
		assertAll("Garantías de seguridad ante parámetros de entrada nulos",
				() ->  assertNotNull(ex, "La excepción lanzada no debe ser vacía"),
				() -> verifyNoInteractions(maquinaDAO)
		);
	}
	
	
	/**
	 * consultarStock_IdValidoMaquinaVacia(): evalúa el comportamiento del sistema ante una máquina sin plan de carga o recién
	 * instalada.
	 * - Técnica aplicada: Análisis de Valores Límite - AVL (frontera inferior absoluta de la colección: tamaño cero).
	 * - Estrategia de verificación: uso combinado de assertDoesNotThrow (la colección vacía es legal y no debe fallar) y 
	 * assertTrue para constatar la ausencia controlada de elementos sin recurror a retornos nulos.
	 **/
	@Test
	@Tag("CajaNegra")
	@DisplayName("ID válido existente pero con la lista de stock vacía")
	void consultarStock_IdValidoMaquinaVacia()
	{
		// [Arrange] Configuración del escenario de máquina recién dada de alta.
		String idMaq = "M-002";
		MaquinaExpendedora maquinaSimulada = mock(MaquinaExpendedora.class);
		
		// Inicializada pero vacía (0 elementos), previniendo errores de recorrido de bucles.
		List<Stock> listaStockVacia = new ArrayList<>();
		
		when(maquinaDAO.buscarPorId(idMaq)).thenReturn(maquinaSimulada);
		when(maquinaSimulada.getListaStock()).thenReturn(listaStockVacia);
		
		// [Act & Assert] Asegurar que la operación finaliza con éxito rotundo (sin lanzar excepciones).
		List<Stock> resultadoReal = assertDoesNotThrow(() -> vendingService.consultarStock(idMaq),
				"El procesamiento de colecciones de tamaño cero debe ser transparente y libre de excepciones");
		
		// [Assert de Estado] Se verifica la consistencia de la lista devuelta.
		assertAll("Auditoría de consistencia para inventarios vacíos",
				() -> assertNotNull(resultadoReal, "El servicio no debe retornar null; debe devolver la colección vacía"),
				() -> assertEquals(0, resultadoReal.size(), "El tamaño de la lista de stocks devuelta debe ser 0"),
				() -> assertTrue(resultadoReal.isEmpty(), "Validación lógica booleana: la lista se encuentra vacía")
		);
	}
	
	
	/**
	 * consultarStock_IdValidoMuelleMinimo(): valida la precisión de datos en el escalón mínimo funcional de la colección.
	 * - Técnica aplicada: Análisis de Valores Límite - AVL (mínimo funcional de elementos de salida: tamaño 1).
	 * - Estrategia de verificación: assertEquals comprueba las dimensiones de la lista y que el único elemento sea accesible.
	 **/
	@Test
	@Tag("CajaNegra")
	@DisplayName("ID válido existente con el mínimo de productos (1)")
	void consultarStock_IdValidoMuelleMinimo() throws Exception
	{
		// [Arrange] Configuración del muelle mínimo.
		String idMaq = "M-003";
		MaquinaExpendedora maquinaSimulada = mock(MaquinaExpendedora.class);
		
		List<Stock> listaStockMinima = new ArrayList<>();
		listaStockMinima.add(mock(Stock.class));	// Inserción del único muelle configurado.
		
		when(maquinaDAO.buscarPorId(idMaq)).thenReturn(maquinaSimulada);
		when(maquinaSimulada.getListaStock()).thenReturn(listaStockMinima);
		
		// [Act] Solicitud del inventario.
		List<Stock> resultadoReal = vendingService.consultarStock(idMaq);
		
		// [Assert] Se evalúa que el sistema responde de forma precisa al límite mínimo.
		assertAll("Verificaciones del límite unitario de stock",
				() -> assertNotNull(resultadoReal, "La lista recuperada no debe ser nula"),
				() -> assertEquals(1, resultadoReal.size(), "La lista debe contener exactamente 1 objeto Stock"),
				() -> assertSame(listaStockMinima.get(0), resultadoReal.get(0), "El elemento recuperado debe ser idéntico al simulado")
		);
	}
	
	
	/**
	 * consultarStock_IdVacio(): comportamiento cuando el identificador de la máquina es una cadena de espacios.
	 * - Técnica aplicada: Caja Blanca (Técnica de McCabe / Cobertura de decisión).
	 * - Estrategia de verificación: análisis de caminos lógicos mediante inyección de parámetro inválido (cadena compuesta por espacios
	 * en blanco). Se verifica mediante assertThrows que el flujo se interrumpe de forma controlada lanzando la excepción 
	 * InvalidIdentifierException al evaluarse como verdadera la segunda condición del filtro de robustez, garantizando además la ausencia
	 * de interacciones residuales con el componente MaquinaDAO.
	 **/
	@Test
	@Tag("CajaBlanca")
	@DisplayName("McCabe: identificador vacío")
	void consultarStock_IdVacio()
	{
		// [Arrange] Se fuerza una cadena con espacios en blanco para activar el id.trim().isEmpty().
		String idVacio = " ";
		
		// [Act & Assert] Caja Blanca: se evalúa la rama que faltaba del control de robustez.
		assertThrows(InvalidIdentifierException.class, () -> {
			vendingService.consultarStock(idVacio);
		}, "El sistema debe detectar cadenas vacías y lanzar InvalidIdentifierException");
	}
}
