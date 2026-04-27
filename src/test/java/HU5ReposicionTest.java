package test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import Entidades.*;
import Servicio.VendingService;
import DAO.*;
import Excepciones.*;
import java.time.LocalDate;
import java.util.List;

class HU5ReposicionTest {

    private MaquinaDAO maquinaDAO;
    private LocalizacionDAO localizacionDAO;
    private VendingService service;

    @BeforeEach
    void setUp() {
        maquinaDAO = new MaquinaDAO();
        localizacionDAO = new LocalizacionDAO();
        service = new VendingService(maquinaDAO, localizacionDAO);
    }

    @Test
    void testProductosBajoStockSeDetectanCorrectamente() throws Exception {
        // Crear máquina
        MaquinaExpendedora maquina = new MaquinaExpendedora();
        maquina.setID("M-001");
        maquina.setNombre("Máquina Principal");
        
        Localizacion loc = new Localizacion();
        loc.setLatitud(40.0);
        loc.setLongitud(-3.0);
        loc.setDireccion("Calle Mayor 1");
        maquina.setLocalizacion(loc);
        maquina.setEstado(Estado.OPERATIVA);

        // Crear producto
        Producto producto = new Producto();
        producto.setId("P-001");
        producto.setNombre("Coca Cola");
        producto.setCategoria("Bebidas");
        producto.setPrecio(2.0);
        producto.setFechaAlta(LocalDate.now().minusDays(10));

        // Crear stock con cantidad por debajo del umbral
        Stock stock = new Stock();
        stock.setProducto(producto);
        stock.setCantidadActual(2);  // Por debajo del umbral de 5
        stock.setCapacidadMax(20);
        stock.setFechaUltimaReposicion(LocalDate.now().minusDays(10));
        stock.setUnidadesVendidas(50);  // 50 unidades vendidas en 10 días
        maquina.addStock(stock);

        // Insertar máquina
        maquinaDAO.insertar(maquina);

        // Consultar productos bajo stock con umbral = 5
        List<Stock> resultado = service.consultarProductosBajoStock(maquina, 5);

        // Verificar que se detecta el producto bajo stock
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("P-001", resultado.get(0).getProducto().getId());
        assertTrue(resultado.get(0).isBajoMinimos(5));
    }

    @Test
    void testCalculoFechaAgotamientoEsCoherente() throws Exception {
        // Crear máquina
        MaquinaExpendedora maquina = new MaquinaExpendedora();
        maquina.setID("M-002");
        maquina.setNombre("Máquina Secundaria");
        
        Localizacion loc = new Localizacion();
        loc.setLatitud(41.0);
        loc.setLongitud(-4.0);
        loc.setDireccion("Calle Secundaria 2");
        maquina.setLocalizacion(loc);
        maquina.setEstado(Estado.OPERATIVA);

        // Crear producto
        Producto producto = new Producto();
        producto.setId("P-002");
        producto.setNombre("Agua");
        producto.setCategoria("Bebidas");
        producto.setPrecio(1.0);
        producto.setFechaAlta(LocalDate.now().minusDays(20));

        // Crear stock
        Stock stock = new Stock();
        stock.setProducto(producto);
        stock.setCantidadActual(30);  // Quedan 30 unidades
        stock.setCapacidadMax(100);
        stock.setFechaUltimaReposicion(LocalDate.now().minusDays(20));
        stock.setUnidadesVendidas(100);  // 100 unidades vendidas en 20 días = 5/día
        maquina.addStock(stock);

        // Calcular fecha de reposición estimada
        LocalDate fechaAgotamiento = maquina.estimarFechaReposicion(stock);

        // Verificar: 30 unidades / 5 unidades/día = 6 días
        // Fecha esperada: hoy + 6 días
        LocalDate fechaEsperada = LocalDate.now().plusDays(6);
        
        assertNotNull(fechaAgotamiento);
        assertEquals(fechaEsperada, fechaAgotamiento);
    }

    @Test
    void testProductosPorEncimaDelUmbralNoSeDetectan() throws Exception {
        // Crear máquina
        MaquinaExpendedora maquina = new MaquinaExpendedora();
        maquina.setID("M-003");
        maquina.setNombre("Máquina Terciaria");
        
        Localizacion loc = new Localizacion();
        loc.setLatitud(42.0);
        loc.setLongitud(-5.0);
        loc.setDireccion("Calle Terciaria 3");
        maquina.setLocalizacion(loc);
        maquina.setEstado(Estado.OPERATIVA);

        // Crear producto
        Producto producto = new Producto();
        producto.setId("P-003");
        producto.setNombre("Snack");
        producto.setCategoria("Alimentos");
        producto.setPrecio(1.5);
        producto.setFechaAlta(LocalDate.now().minusDays(5));

        // Crear stock con cantidad por ENCIMA del umbral
        Stock stock = new Stock();
        stock.setProducto(producto);
        stock.setCantidadActual(15);  // Por encima del umbral de 5
        stock.setCapacidadMax(50);
        stock.setFechaUltimaReposicion(LocalDate.now().minusDays(5));
        stock.setUnidadesVendidas(25);
        maquina.addStock(stock);

        // Consultar productos bajo stock con umbral = 5
        List<Stock> resultado = service.consultarProductosBajoStock(maquina, 5);

        // Verificar que NO se detecta (lista vacía)
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void testMaquinaNulaLanzaExcepcion() {
        // Verificar que consultar con máquina nula lanza EntityNotFoundException
        Exception exception = assertThrows(EntityNotFoundException.class, () -> {
            service.consultarProductosBajoStock(null, 5);
        });
        
        assertEquals("Máquina no encontrada.", exception.getMessage());
    }

    @Test
    void testIsBajoMinimosFuncionaCorrectamente() {
        Stock stock = new Stock();
        stock.setCantidadActual(3);
        
        assertTrue(stock.isBajoMinimos(5));  // 3 < 5
        assertFalse(stock.isBajoMinimos(3)); // 3 == 3 (no es menor)
        assertFalse(stock.isBajoMinimos(1)); // 3 > 1
    }

    @Test
    void testVelocidadConsumoCeroNoProduceError() throws Exception {
        // Crear máquina
        MaquinaExpendedora maquina = new MaquinaExpendedora();
        maquina.setID("M-004");
        maquina.setNombre("Máquina Zero");
        
        Localizacion loc = new Localizacion();
        loc.setLatitud(43.0);
        loc.setLongitud(-6.0);
        loc.setDireccion("Calle Zero 4");
        maquina.setLocalizacion(loc);
        maquina.setEstado(Estado.OPERATIVA);

        // Crear producto
        Producto producto = new Producto();
        producto.setId("P-004");
        producto.setNombre("Zero Ventas");
        producto.setCategoria("Bebidas");
        producto.setPrecio(2.0);
        producto.setFechaAlta(LocalDate.now().minusDays(10));

        // Crear stock con 0 unidades vendidas
        Stock stock = new Stock();
        stock.setProducto(producto);
        stock.setCantidadActual(10);
        stock.setCapacidadMax(50);
        stock.setFechaUltimaReposicion(LocalDate.now().minusDays(10));
        stock.setUnidadesVendidas(0);  // Sin ventas
        maquina.addStock(stock);

        // Calcular fecha de reposición - debe retornar null por velocidad 0
        LocalDate fechaAgotamiento = maquina.estimarFechaReposicion(stock);
        
        assertNull(fechaAgotamiento);
    }
}
