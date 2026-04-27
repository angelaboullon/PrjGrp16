import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Entidades.*;
import DAO.*;
import Excepciones.*;
import Servicio.*;

import java.util.List;

class HU3StockTest {

    private VendingService servicio;
    private MaquinaDAO maquinaDAO;
    private LocalizacionDAO localizacionDAO;
    private MaquinaExpendedora maquina;
    private Producto producto;

    @BeforeEach
    void setUp() throws Exception {
        maquinaDAO = new MaquinaDAO();
        localizacionDAO = new LocalizacionDAO();
        servicio = new VendingService(maquinaDAO, localizacionDAO);

        // Crear una máquina de prueba
        maquina = new MaquinaExpendedora();
        maquina.setID("M-001");
        maquina.setNombre("Máquina Test");

        // Crear una localización de prueba
        Localizacion localizacion = new Localizacion();
        localizacion.setDireccion("Calle Test, 1, 1A, Ciudad Test, 12345");
        localizacion.setLatitud(40.0);
        localizacion.setLongitud(-3.0);

        maquina.setLocalizacion(localizacion);
        maquina.setEstado(Estado.OPERATIVA);

        // Crear un producto de prueba
        producto = new Producto();
        producto.setId("P-001");
        producto.setNombre("Producto Test");
        producto.setPrecio(1.50);

        // Añadir stock a la máquina
        Stock stock = new Stock();
        stock.setProducto(producto);
        stock.setCantidadActual(10);
        stock.setCapacidadMax(50);
        maquina.addStock(stock);

        // Insertar la máquina en el DAO
        maquinaDAO.insertar(maquina);
    }

    @Test
    void testConsultarStock_CasoDeExito() throws Exception {
        // Act
        List<Stock> resultado = servicio.consultarStock(maquina);

        // Assert
        assertNotNull(resultado, "La lista de stock no debe ser nula");
        assertEquals(1, resultado.size(), "Debe haber exactamente 1 elemento en el stock");
        
        Stock stockRecuperado = resultado.get(0);
        assertEquals(producto, stockRecuperado.getProducto(), "El producto debe coincidir");
        assertEquals(10, stockRecuperado.getCantidadActual(), "La cantidad actual debe ser 10");
        assertEquals(50, stockRecuperado.getCapacidadMax(), "La capacidad máxima debe ser 50");
    }

    @Test
    void testConsultarStock_MaquinaNula() {
        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> servicio.consultarStock(null),
            "Se esperaba EntityNotFoundException al pasar una máquina nula"
        );
        
        assertTrue(exception.getMessage().contains("nula"), 
            "El mensaje debe indicar que la máquina es nula");
    }

    @Test
    void testConsultarStock_MaquinaNoEncontrada() throws Exception {
        // Arrange: Crear una máquina que NO está en el DAO
        MaquinaExpendedora maquinaNoExistente = new MaquinaExpendedora();
        maquinaNoExistente.setID("M-999");
        maquinaNoExistente.setNombre("Máquina No Existente");

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> servicio.consultarStock(maquinaNoExistente),
            "Se esperaba EntityNotFoundException para máquina no encontrada"
        );
        
        assertTrue(exception.getMessage().contains("no encontrada") || 
                   exception.getMessage().contains("M-999"), 
            "El mensaje debe indicar que la máquina no fue encontrada");
    }

    @Test
    void testConsultarStock_ListaVacia() throws Exception {
        // Arrange: Crear una máquina sin stock
        MaquinaExpendedora maquinaSinStock = new MaquinaExpendedora();
        maquinaSinStock.setID("M-002");
        maquinaSinStock.setNombre("Máquina Sin Stock");
        
        Localizacion localizacion2 = new Localizacion();
        localizacion2.setDireccion("Calle 2, Ciudad 2, 23456");
        localizacion2.setLatitud(41.0);
        localizacion2.setLongitud(-4.0);
        
        maquinaSinStock.setLocalizacion(localizacion2);
        maquinaSinStock.setEstado(Estado.OPERATIVA);
        
        maquinaDAO.insertar(maquinaSinStock);

        // Act
        List<Stock> resultado = servicio.consultarStock(maquinaSinStock);

        // Assert
        assertNotNull(resultado, "La lista de stock no debe ser nula");
        assertTrue(resultado.isEmpty(), "La lista de stock debe estar vacía");
    }

    @Test
    void testConsultarStock_VariosElementos() throws Exception {
        // Arrange: Añadir más stock a la máquina existente
        Producto producto2 = new Producto();
        producto2.setId("P-002");
        producto2.setNombre("Producto Test 2");
        producto2.setPrecio(2.00);

        Stock stock2 = new Stock();
        stock2.setProducto(producto2);
        stock2.setCantidadActual(5);
        stock2.setCapacidadMax(30);
        maquina.addStock(stock2);

        // Act
        List<Stock> resultado = servicio.consultarStock(maquina);

        // Assert
        assertNotNull(resultado, "La lista de stock no debe ser nula");
        assertEquals(2, resultado.size(), "Debe haber exactamente 2 elementos en el stock");
    }
}
