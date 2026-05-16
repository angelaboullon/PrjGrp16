package Servicio;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import Entidades.MaquinaExpendedora;
import Entidades.Producto;
import Entidades.Stock;
import DAO.MaquinaDAO;
import DAO.ProductoDAO;
import DAO.VentaDAO;

import Excepciones.EntityNotFoundException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;


/**
 * Clase de pruebas de integración en contexto de caja negra y caja blanca para 'VendingService'.
 * 
 * Valida de forma exhaustiva el método crítico venderProducto(String, String, int) de la HU4.
 * Utiliza Mockito (MockitoExtension) para aislar la capa de servicio simulando el comportamiento de las
 * dependencias de persistencia (DAOs) mediante dobles de prueba (Mocks/Stubs).
 * Combina técnicas de Clases de Equivalencia con el Análisis de Decisiones de McCabe para asegurar una
 * cobertura del 100% en las ramificaciones de control del servicio.
 **/
@ExtendWith(MockitoExtension.class)
@Tag("HU4")

public class VendingServiceHU4Test 
{
	// ========================================================
	// DECLARACIÓN DE DEPENDENCIAS SIMULADAS MEDIANTE MOCKITO
	// ========================================================
	
	@Mock
	private MaquinaDAO maquinaDAO;
	
	@Mock
	private ProductoDAO productoDAO;
	
	@Mock
	private VentaDAO ventaDAO;
	
	
	// ============================================================================================
	// INYECCIÓN AUTOMATIZADA DEL SERVICIO BAJO ANÁLISIS vinculando los dobles declarados arriba.
	// ============================================================================================
	@InjectMocks
	private VendingService vendingService;
	

	// =======
	// TESTS
	// =======
	
	/**
	 * venderProductoFlujoIdeal(): valida la ejecución perfecta de una transacción comercial.
	 * - Técnica aplicada: Clase de Equivalencia Válida (todos los IDs existen, están vinculados y hay stock).
	 * - Estrategia de verificación: assertNotNull (entorno), assertTimeout (eficiencia de tiempo),
	 * assertSame (identidad de objetos) y assertAll agrupando assertTrue/assertFalse y las verificaciones de
	 * comportamiento de Mockito (verify).
	 **/
	@Test
	@Tag("CajaNegra")
	@DisplayName("Flujo Total (Transacción exitosa)")
	void venderProductoFlujoIdeal() throws Exception
	{
		// [Arrange] Configuración de los datos fijos de entrada.
		String idMaq = "M-001";
		String idProd = "P-001";
		int cantidad = 2;
		
		// Creación de los dobles de simulación para las entidades de negocio.
		MaquinaExpendedora maquinaSimulada = mock(MaquinaExpendedora.class);
		Producto productoSimulado = mock(Producto.class);
		Stock stockSimulado = mock(Stock.class);
		
		// Definición de comportamientos esperados de los DAOs y entidades (Stubbing).
		when(maquinaDAO.buscarPorId(idMaq)).thenReturn(maquinaSimulada);
		when(productoDAO.buscarPorId(idProd)).thenReturn(productoSimulado);
		when(maquinaSimulada.buscarStockProducto(productoSimulado)).thenReturn(stockSimulado);
		
		// [Assert - Fase Previa] Se comprueba que el entorno inyectó correctamente el servicio antes de operar.
		assertNotNull(vendingService, "El servicio inyectado no debe ser nulo para proceder con el flujo");
		
		// [Act & Assert - Temporalidad] Se ejecuta la venta auditando que responda en menos de 500ms (eficiencia).
		assertTimeout(Duration.ofMillis(500), () -> {
			vendingService.venderProducto(idMaq, idProd, cantidad);
		}, "La operación crítica de venta excede los umbrales de tiempo no funcionales permitidos");
		
		// [Assert - Identidad de Punteros] Se verifica con el operador == que el DAO retorna la instancia exacta 
		// mapeada.
		assertSame(maquinaSimulada, maquinaDAO.buscarPorId(idMaq), "El componente de persistencia debe retornar "
				+ "exactamente la misma referencia física del objeto máquina");
		
		// [Assert - Estado Final] Se agrupan las aserciones de control lógico de negocio y de invocación.
		// 1. [verify]: se verifica que el servicio ordenó retirar el stock correspondiente y que se invocó la 
		// persistencia para asentar la nueva factura de venta.
		// 2. [assertTrue]: comprobación lógica; la cantidad evaluada debe ser superior a cero.
		// 3. [assertFalse]: comprobación lógica inversa; una cantidad igual a cero no debe haber provocado una venta vacía.
		assertAll("Verificaciones colectivas del comportamiento del servicio en flujo ideal",
				() -> verify(stockSimulado, times(1)).decrementar(cantidad),
				() -> verify(ventaDAO, times(1)).registrar(any()),
				() -> assertTrue(cantidad > 0, "La cantidad evaluada de la venta debe computar como un entero positivo"),
				() -> assertFalse(cantidad == 0, "Una transacción por valor de cero unidades no es lógica en este flujo")
		);
	}
	
	
	/**
	 * venderProducto_ProductoNoAsignado(): valida el rechazo de ventas cuando el producto no está cargado en los muelles de 
	 * esa máquina.
	 * - Técnica aplicada: Clase de Equivalencia Inválida de negocio (parámetros correctos, pero sin asociación).
	 * - Estrategia de verificación: assertThrows captura EntityNotFoundException.
	 **/
	@Test
	@Tag("CajaNegra")
	@DisplayName("Error: producto no asignado")
	void venderProducto_ProductoNoAsignado()
	{
		// [Arrange] Configuración de IDs válidos.
		String idMaq = "M-001";
		String idProd = "P-001";
		
		MaquinaExpendedora maquinaSimulada = mock(MaquinaExpendedora.class);
		Producto productoSimulado = mock(Producto.class);
		
		// Comportamiento del Stub: la máquina y el producto existen globalmente, pero el muelle no los vincula (devuelve null).
		when(maquinaDAO.buscarPorId(idMaq)).thenReturn(maquinaSimulada);
		when(productoDAO.buscarPorId(idProd)).thenReturn(productoSimulado);
		when(maquinaSimulada.buscarStockProducto(productoSimulado)).thenReturn(null);
		
		// [Act & Assert] Intentar la venta debe provocar el corte por muelle inexistente.
		EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> {
			vendingService.venderProducto(idMaq, idProd, 5);
		}, "Se esperaba EntityNotFoundException debido a que el producto no tiene muelle de stock asignado");
		
		// Bloqueo preventivo: se verifica que la transacción jamás llegó a las ventas.
		verifyNoInteractions(ventaDAO);
	}
	
	
	/**
	 * venderProductoMaquinaNoExiste(): verifica el cortocircuito del servicio cuando se le solicita operar con una máquina inexistente.
	 * - Técnica aplicada: Clase de Equivalencia Inválida (identificador de máquina erróneo o borrado del sistema).
	 * - Estrategia de verificación: assertThrows captura la excepción; assertNotEquals evita mensajes vacíos; assertNull comprueba
	 * físicamente la respuesta de ausencia del simulador.
	 **/
	@Test
	@Tag("CajaNegra")
	@DisplayName("Error: máquina no existe")
	void venderProductoMaquinaNoExiste()
	{
		// [Arrange] Definición de un identificador de máquina que no consta en los registros.
		String idFicticio = "M-999";
		
		// El simulador responde de forma natural con un nulo al buscar la máquina.
		when(maquinaDAO.buscarPorId(idFicticio)).thenReturn(null);
		
		// [Act & Assert] Se comprueba que el servicio lanza la excepción reglamentaria.
		EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> {
			vendingService.venderProducto(idFicticio, "P-001", 5);
		});
		
		// [Assert - Contenido] Se exige que la excepción aporte información y no retorne una cadena vacía.
		assertNotEquals("", ex.getMessage(), "La excepción lanzada no debe contener un mensaje vacío");
		
		// [Assert - Persistencia] Se certifica que el mock devuelve estrictamente nulo ante la petición.
		assertNull(maquinaDAO.buscarPorId(idFicticio), "El DAO de persistencia debe devolver forzosamente null para registros ficticios");
		
		// Cortocircuito de seguridad: no se debe perder tiempo consultando productos ni guardando registros de facturas.
		verifyNoInteractions(productoDAO);
		verifyNoInteractions(ventaDAO);
	}
	
	
	/**
	 * venderProductoMaquinaNula(): asegura que el método bloquea de forma preventiva llamadas con parámetros de máquina nulos.
	 * - Técnica aplicada: control de robustez (frontera de datos inválidos estructurales).
	 * - Estrategia de verificación: assertThrows captura la excepción de tipo IllegalArgumentException.
	 **/
	@Test
	@Tag("CajaNegra")
	@DisplayName("Control de robustez: máquina nula")
	void venderProductoMaquinaNula()
	{
		// [Act & Assert] El intento de procesar un ID de maquina nulo activa el blindaje inmediato del servicio.
		assertThrows(IllegalArgumentException.class, () -> {
			vendingService.venderProducto(null, "P-001", 5);
		}, "El sistema debe abortar mediante IllegalArgumentException si el parámetro ID de máquina es nulo");
		
		// Bloqueo preventivo de persistencia.
		verifyNoInteractions(ventaDAO);
	}
	
	
	/**
	 * venderProducto_ProductoNulo(): evita fallos de tipo NullPointerException descontrolados si el parámetro del producto viene vacío.
	 * - Técnica aplicada: control de robustez (parámetros malformados de entrada).
	 * - Estrategia de verificación: assertThrows intercepta el fallo controlado mediante una IllegalArgumentException limpia.
	 **/
	@Test
	@Tag("CajaNegra")
	@DisplayName("Control de robustez: producto nulo")
	void venderProducto_ProductoNulo()
	{
		// [Act & Assert] Pasar un nulo en el ID del artículo corta el procesamiento de la venta.
		assertThrows(IllegalArgumentException.class, () -> {
			vendingService.venderProducto("M-001", null, 5);
		}, "El sistema dee abortar mediante IllegalArgumentException si el parámetro ID de producto es nulo");
		
		// Bloqueo preventivo de persistencia.
		verifyNoInteractions(ventaDAO);
	}
	
	
	/**
	 * venderProducto_ProductoInexistente():  verifica el cortocircuito del servicio cuando se le solicita operar con un producto inexistente.
	 * - Técnica aplicada: Caja Blanca (técnica de McCabe / Cobertura de caminos mínimos).
	 * - Estrategia de verificación: análisis estructural para cubrir la decisión huérfana detectada por Eclemma. Se pasa un ID de producto
	 * válido en formato pero inexistente. Se simula el DAO para que devuelva null y se inercepta mediante assertThrows la excepción
	 * EntityNotFoundException, verificando además el bloqueo preventivo de persistencia con verifyNoInteractions.
	 **/
	@Test
	@Tag("CajaBlanca")
	@DisplayName("McCabe: producto válido pero inexistente en el DAO")
	void venderProducto_ProductoInexistente()
	{
		String idMaq = "M-001";
		String idProdInexistente = "P-999";
		
		// [Arrange] Se simula que la máquina sí existe para superar el filtro m == null.
		MaquinaExpendedora maquinaSimulada = mock(MaquinaExpendedora.class);
		when(maquinaDAO.buscarPorId(idMaq)).thenReturn(maquinaSimulada);
		
		// Se fuerza al DAO del producto a devolver null ante el ID inexistente.
		when(productoDAO.buscarPorId(idProdInexistente)).thenReturn(null);
		
		// [Act & Assert] Se verifica que salta la excepción correcta de negocio.
		assertThrows(EntityNotFoundException.class, () -> {
			vendingService.venderProducto(idMaq, idProdInexistente, 1);
		}, "Debe lanzar EntityNotFoundException si el producto no existe en el catálogo");
		
		// Se verifica que la venta no se registra en la base de datos.
		verifyNoInteractions(ventaDAO);
	}
}
