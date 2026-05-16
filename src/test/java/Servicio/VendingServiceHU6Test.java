package Servicio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import DAO.VentaDAO;
import Entidades.MaquinaExpendedora;
import Entidades.Producto;
import Entidades.Stock;
import Entidades.Venta;

@DisplayName("PR-HU6-01: Cálculo de Fecha Límite de Reposición")
class VendingServiceHU6Test {

    AutoCloseable acl;

    @Mock
    VentaDAO ventaDAO;

    @InjectMocks
    VendingService servicio;

    MaquinaExpendedora maquina;
    Producto producto;
    Stock stock;

    @BeforeEach
    void setUp() throws Exception {
        acl = MockitoAnnotations.openMocks(this);
        
        maquina = new MaquinaExpendedora();
        maquina.setID("M-100");

        // Recuerda: ID válido de 3 dígitos para evitar InvalidIdentifierException
        producto = new Producto();
        producto.setId("P-099"); 
        producto.setNombre("Barrita Energética");

        stock = new Stock();
        stock.setProducto(producto);
        stock.setCantidadActual(50); 
    }

    @AfterEach
    void tearDown() throws Exception {
        acl.close();
    }

    // =================================================================
    // FASE 1: CAJA NEGRA (Funcionalidad principal)
    // =================================================================

    @Test
    @Tag("CajaNegra")
    @DisplayName("CP-61: Consumo Normal (Proyección estándar)")
    void testEstimarFechaReposicion_ConsumoNormal() {
        // Arrange
        stock.setFechaUltimaReposicion(LocalDate.now().minusDays(10));
        maquina.addStock(stock);

        List<Venta> listaVentas = new ArrayList<>();
        listaVentas.add(new Venta("M-100", "P-099", 15, LocalDateTime.now().minusDays(5)));
        listaVentas.add(new Venta("M-100", "P-099", 15, LocalDateTime.now().minusDays(2)));

        when(ventaDAO.buscarDesdeFecha(eq("M-100"), eq("P-099"), any(LocalDateTime.class)))
            .thenReturn(listaVentas);

        // Act
        LocalDate fechaCalculada = servicio.estimarFechaReposicion(maquina, producto);

        // Assert: 30 uds en 10 días = 3/día. 50/3 = 16 días. 16 - 1 (margen) = 15 días desde hoy.
        LocalDate fechaEsperada = LocalDate.now().plusDays(15);
        assertEquals(fechaEsperada, fechaCalculada);
        
        verify(ventaDAO, times(1)).buscarDesdeFecha(anyString(), anyString(), any(LocalDateTime.class));
    }

    @Test
    @Tag("CajaNegra")
    @DisplayName("CP-62: Producto No Asignado (Retorna Null)")
    void testEstimarFechaReposicion_ProductoNoExiste() {
        // Arrange: NO añadimos el stock a la máquina (está vacía)

        // Act
        LocalDate fechaCalculada = servicio.estimarFechaReposicion(maquina, producto);

        // Assert
        assertNull(fechaCalculada, "Si el producto no está asignado a la máquina, debe devolver null.");
        verify(ventaDAO, never()).buscarDesdeFecha(anyString(), anyString(), any(LocalDateTime.class));
    }

}