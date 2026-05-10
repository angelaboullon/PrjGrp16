# Documentación de Pruebas: Historia de Usuario 5 (Filtrado de Stock por Umbral)

## 1. Procedimiento de Ejecución
Siguiendo las instrucciones para la implementación de las pruebas correspondientes a la HU5, se han llevado a cabo las siguientes acciones:

1. **Gestión de control de versiones:**
   - Sincronización de la rama principal (`git fetch` / `git pull origin main`).
   - Creación de una rama aislada dedicada exclusivamente a las pruebas para no afectar la rama de desarrollo principal. La rama se ha denominado `feature/pruebas-hu5-angelaboullon`.

2. **Desarrollo de las Pruebas (TDD / Unit Testing):**
   - Se ha creado la clase de test `VendingServiceHU5Test.java` bajo la ruta `src/test/java/Servicio/`.
   - Se ha diseñado utilizando el framework estándar **JUnit 5**, respetando la estructura con las anotaciones `@BeforeEach` para inicializar el contexto (escenario) antes de cada método de test.
   - En este caso no se necesitaron Mocks (`@Mock` o `Mockito`) debido a que el método a testear (`consultarProductosBajoStock`) interactúa directamente con los datos de las entidades instanciadas en memoria (`MaquinaExpendedora`, `Stock`) y no realiza peticiones a la capa de persistencia (Base de Datos). 

3. **Subpruebas Implementadas:**
   - *Valores Límite (AVL) y Particiones de Equivalencia:* Se testearon los valores frontera configurando productos con stock de `4` (Límite Inferior - Alerta), `5` (Frontera) y `10` (Superior). Además, se añadió la aserción ante una lista vacía para corroborar el correcto estado del bucle.
   - *Cobertura Lógica (McCabe):* Asegurando que el camino del bucle se ejecute en `true` y `false` para alcanzar el 100% de cobertura en la jacoco/EclEmma.

## 2. Código Integrado

A continuación se expone la implementación oficial de la clase `VendingServiceHU5Test`:

```java
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
        servicio = new VendingService();
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
        int umbral = 5;

        // Límite Inferior (Alerta)
        Stock stockInferior = new Stock();
        Producto p1 = new Producto();
        try { p1.setId("P-001"); } catch (Exception e) {}
        stockInferior.setProducto(p1);
        stockInferior.setCantidadActual(4); // < 5

        // Límite Exacto (Frontera)
        Stock stockFrontera = new Stock();
        Producto p2 = new Producto();
        try { p2.setId("P-002"); } catch (Exception e) {}
        stockFrontera.setProducto(p2);
        stockFrontera.setCantidadActual(5); // == 5

        // Superior (Seguro)
        Stock stockSuperior = new Stock();
        Producto p3 = new Producto();
        try { p3.setId("P-003"); } catch (Exception e) {}
        stockSuperior.setProducto(p3);
        stockSuperior.setCantidadActual(10); // > 5

        maquina.addStock(stockInferior);
        maquina.addStock(stockFrontera);
        maquina.addStock(stockSuperior);

        List<Stock> result = servicio.consultarProductosBajoStock(maquina, umbral);

        assertNotNull(result, "La lista devuelta no debe ser nula.");
        assertEquals(1, result.size(), "Solo debería haber 1 producto por debajo del umbral.");
        assertEquals("P-001", result.get(0).getProducto().getId(), "Debe ser el de cantidad 4.");
    }

    @Test
    @DisplayName("PR-HU5-01. Subprueba Lista Vacía")
    void testFiltradoBajoStock_ListaVacia() {
        int umbral = 5;
        List<Stock> result = servicio.consultarProductosBajoStock(maquina, umbral);
        assertNotNull(result, "El método debe devolver una lista vacía, no null.");
        assertTrue(result.isEmpty(), "La lista debe estar vacía.");
    }

    @Test
    @DisplayName("PR-HU5-02. Cobertura Lógica del Filtrado (Caja Blanca / McCabe)")
    void testCoberturaLogicaFiltrado() {
        int umbral = 5;
        // Camino 1: Bucle intacto
        List<Stock> path1Result = servicio.consultarProductosBajoStock(maquina, umbral);
        assertTrue(path1Result.isEmpty(), "Camino 1: Lista vacía procesada.");

        Stock stockBajo = new Stock();
        Producto pBajo = new Producto();
        try { pBajo.setId("P-004"); } catch(Exception e) {}
        stockBajo.setProducto(pBajo);
        stockBajo.setCantidadActual(3); // true

        Stock stockAlto = new Stock();
        Producto pAlto = new Producto();
        try { pAlto.setId("P-005"); } catch(Exception e) {}
        stockAlto.setProducto(pAlto);
        stockAlto.setCantidadActual(8); // false

        maquina.addStock(stockBajo);
        maquina.addStock(stockAlto);

        List<Stock> resultCoverage = servicio.consultarProductosBajoStock(maquina, umbral);

        assertTrue(resultCoverage.contains(stockBajo), "Camino 2: Entra en el if (true).");
        assertFalse(resultCoverage.contains(stockAlto), "Camino 3: No entra en el if (false).");
        assertEquals(1, resultCoverage.size());
    }
}
```

## 3. Resultados Obtenidos

- **PR-HU5-01 (Caja Negra e Integración):** **PASO** (Verde). La extracción filtra correctamente y es estricta con la condición `< umbral`. Los índices de las listas responden sin problemas (no arrojando nulos y filtrando los excesos/empatados).
- **PR-HU5-02 (Caja Blanca):** **PASO** (Verde). Tal y como marca el criterio de la complejidad ciclomática de McCabe (3 caminos primarios):
   - Flujo 1: (Stock vacío) devuelve lista size = 0.
   - Flujo 2: (Stock por debajo de mínimos) cruza y apenda a `critica`.
   - Flujo 3: (Stock intacto) puentea el control limitador de if y continúa.
- **Cobertura Final en EclEmma:** `100%` en el método para los flujos testeados. No existen desbordamientos ni *NullPointerExceptions* en los registros de objetos subyacentes.