package Servicio;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import Entidades.MaquinaExpendedora;
import Entidades.Producto;
import Entidades.Stock;

class VendingServiceHU5Test {

    private VendingService servicio;
    private MaquinaExpendedora maquina;

    @BeforeEach
    void setUp() {
        servicio = new VendingService(); // No necesitamos Mocks de DAO para este test
        maquina = new MaquinaExpendedora();
        try {
            maquina.setID("M-001");
            maquina.setNombre("Maquina Central");
            maquina.setCapacidad(100);
        } catch(Exception e) {}
    }

    @Test
    @DisplayName("PR-HU5-01. Subpruebas Límite y Frontera (Caja Negra)")
    void testFiltradoBajoStock_ValoresLimite() {
        // Arrange
        int umbral = 5;

        // Subprueba Límite Inferior (Alerta): Cantidad = 4. Debe incluirse.
        Stock stockInferior = new Stock();
        Producto p1 = new Producto();
        try {
            p1.setId("P-001");
            p1.setNombre("Agua Mineral");
            p1.setPrecio(1.0f);
        } catch (Exception e) {}
        stockInferior.setProducto(p1);
        stockInferior.setCantidadActual(4); // < 5

        // Subprueba Límite Exacto (Frontera): Cantidad = 5. No debe incluirse.
        Stock stockFrontera = new Stock();
        Producto p2 = new Producto();
        try {
            p2.setId("P-002");
            p2.setNombre("Refresco");
            p2.setPrecio(1.5f);
        } catch (Exception e) {}
        stockFrontera.setProducto(p2);
        stockFrontera.setCantidadActual(5); // == 5

        // Subprueba Superior (Seguro): Cantidad = 10. No debe incluirse.
        Stock stockSuperior = new Stock();
        Producto p3 = new Producto();
        try {
            p3.setId("P-003");
            p3.setNombre("Snack Salado");
            p3.setPrecio(2.0f);
        } catch (Exception e) {}
        stockSuperior.setProducto(p3);
        stockSuperior.setCantidadActual(10); // > 5

        // Simular que el stock fue añadido a la máquina
        List<Stock> mockObjetoStockMachine = new ArrayList<>();
        mockObjetoStockMachine.add(stockInferior);
        mockObjetoStockMachine.add(stockFrontera);
        mockObjetoStockMachine.add(stockSuperior);

        // Agregamos stocks a la máquina (dependiendo de la implementación de addStock o inyectando la lista)
        // Para asegurar compatibilidad sin reescribir la máquina, iteramos el add
        for (Stock s : mockObjetoStockMachine) {
            maquina.addStock(s);
        }

        // Act
        List<Stock> result = servicio.consultarProductosBajoStock(maquina, umbral);

        // Assert
        assertNotNull(result, "La lista devuelta no debe ser nula.");
        assertEquals(1, result.size(), "Solo debería haber 1 producto por debajo del umbral.");
        assertEquals("P-01", result.get(0).getProducto().getId(), "El producto devuelto debe ser el de cantidad 4.");
    }

    @Test
    @DisplayName("PR-HU5-01. Subprueba Lista Vacía")
    void testFiltradoBajoStock_ListaVacia() {
        // Arrange: la máquina no tiene stocks añadidos (lista vacía)
        int umbral = 5;

        // Act
        List<Stock> result = servicio.consultarProductosBajoStock(maquina, umbral);

        // Assert
        assertNotNull(result, "El método debe devolver una lista vacía, no null.");
        assertTrue(result.isEmpty(), "La lista debe estar vacía cuando la máquina no tiene productos.");
    }

    @Test
    @DisplayName("PR-HU5-02. Cobertura Lógica del Filtrado (Caja Blanca / McCabe)")
    void testCoberturaLogicaFiltrado() {
        int umbral = 5;

        // Camino 1: El bucle no se ejecuta (lista de stock vacía).
        List<Stock> path1Result = servicio.consultarProductosBajoStock(maquina, umbral);
        assertTrue(path1Result.isEmpty(), "Camino 1: Lista vacía procesada.");

        // Configuramos productos para evaluar Caminos 2 y 3.
        Stock stockBajo = new Stock();
        Producto pBajo = new Producto();
        try { pBajo.setId("P-004"); } catch(Exception e) {}
        stockBajo.setProducto(pBajo);
        stockBajo.setCantidadActual(3); // s.isBajoMinimos(5) -> true

        Stock stockAlto = new Stock();
        Producto pAlto = new Producto();
        try { pAlto.setId("P-005"); } catch(Exception e) {}
        stockAlto.setProducto(pAlto);
        stockAlto.setCantidadActual(8); // s.isBajoMinimos(5) -> false

        maquina.addStock(stockBajo);
        maquina.addStock(stockAlto);

        // Act: Ejecutamos el método que internamente evaluará la condición if a true y false
        List<Stock> resultCoverage = servicio.consultarProductosBajoStock(maquina, umbral);

        // Assert
        // Camino 2: El if evalúa a true y se añade a la lista 'critica'
        assertTrue(resultCoverage.contains(stockBajo), "Camino 2: El bucle se ejecuta y entra en el if (true).");
        
        // Camino 3: El if evalúa a false y NO se añade a la lista
        assertFalse(resultCoverage.contains(stockAlto), "Camino 3: El bucle se ejecuta y no entra en el if (false).");
        assertEquals(1, resultCoverage.size(), "Solo debe contener el elemento menor al umbral");
    }
}
