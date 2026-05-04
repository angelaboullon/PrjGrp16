package Servicio;

import Entidades.*;
import DAO.*;
import Excepciones.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class VendingService 
{
    private MaquinaDAO maquinaDAO;
    private LocalizacionDAO localizacionDAO;
    private ProductoDAO productoDAO;
    private VentaDAO ventaDAO;

    // Constructor: Solo necesitamos los DAOs de máquinas y localizaciones para HU1
    public VendingService(MaquinaDAO mDao, LocalizacionDAO lDao, ProductoDAO pDao, VentaDAO vDao) 
    {
        this.maquinaDAO = mDao;
        this.localizacionDAO = lDao;
        this.productoDAO = pDao;
        this.ventaDAO = vDao;
    }

    // HU1 - Cargar máquinas en el sistema: valida que las coordenadas no existan previamente.
    public void registrarLocalizacion(Localizacion l) throws DuplicateLocationException {
        if (localizacionDAO.buscarPorCoordenadas(l.getLatitud(), l.getLongitud()) != null) {
            throw new DuplicateLocationException("Ya existe una localización en estas coordenadas.");
        }
        localizacionDAO.insertar(l);
    }

    // HU1 - Cargar máquinas en el sistema: orquestador principal que valida duplicados y vincula la
    // máquina a una ubicación.
    public void darAltaMaquina(MaquinaExpendedora m, Localizacion l) throws Exception {
        // 1. Intentar registrar localización (si no existe ya)
        try {
            registrarLocalizacion(l);
        } catch (DuplicateLocationException e) {
            // Si ya existe, se permite continuar para asociar la máquina a ella
        }

        // 2. Regla de negocio: Comprobar si la localización ya tiene una máquina asignada
        for (MaquinaExpendedora maq : maquinaDAO.listarTodas()) {
            if (maq.getLocalizacion().getLatitud() == l.getLatitud() && 
                maq.getLocalizacion().getLongitud() == l.getLongitud()) {
                throw new LocationOccupiedException("Esta localización ya tiene una máquina asignada.");
            }
        }

        // 3. Validar que el ID y el Nombre de la máquina sean únicos en el sistema
        if (maquinaDAO.buscarPorId(m.getId()) != null) {
            throw new DuplicateIdentifierException("ID de máquina repetido.");
        }
        
        // Asignación y guardado
        m.setLocalizacion(l);
        maquinaDAO.insertar(m);
    }

    // HU3 - Consultar stock de una máquina
    public List<Stock> consultarStock(MaquinaExpendedora m) throws EntityNotFoundException {
        if (m == null) {
            throw new EntityNotFoundException("La máquina es nula.");
        }
        MaquinaExpendedora maquina = maquinaDAO.buscarPorId(m.getId());
        if (maquina == null) {
            throw new EntityNotFoundException("Máquina no encontrada en el sistema: " + m.getId());
        }
        return maquina.getListaStock();
    }

    // Gestión de Catálogo
    public void darAltaProducto(Producto p) throws Exception {
        if (productoDAO.buscarPorId(p.getId()) != null) throw new DuplicateIdentifierException("ID de producto repetido.");
        if (productoDAO.buscarPorNombre(p.getNombre()) != null) throw new DuplicateNameException("Nombre de producto repetido.");
        productoDAO.insertar(p);
    }

    // HU2 - Asociar productos a máquina: crea la relación entre un producto y una máquina, validando
    // que la máquina no supere su capacidadTotal.
    public void asignarProductoMaquina(MaquinaExpendedora m, Producto p, int cupoMax) throws Exception {
        // Validar existencia
        if (maquinaDAO.buscarPorId(m.getId()) == null || productoDAO.buscarPorId(p.getId()) == null) {
            throw new EntityNotFoundException("La máquina o el producto no existen.");
        }
        // Validar si ya está asignado
        if (m.buscarStockProducto(p) != null) {
            throw new ProductAlreadyAssignedException("El producto ya está en esta máquina.");
        }
        // Validar espacio físico total de la máquina
        if (m.calcularEspacioOcupado() + cupoMax > m.getCapacidad()) {
            throw new CapacityExceededException("No hay espacio suficiente en la máquina para ese cupo.");
        }

        Stock nuevoStock = new Stock();
        nuevoStock.setProducto(p);
        nuevoStock.setCapacidadMax(cupoMax);
        nuevoStock.setCantidadActual(0);
        m.addStock(nuevoStock);
    }

    // HU2: Reposición
    public void reponerStock(MaquinaExpendedora m, Producto p, int cantidad) throws Exception {
        Stock s = m.buscarStockProducto(p);
        if (s == null) throw new EntityNotFoundException("El producto no está asignado a esta máquina.");
        
        s.incrementar(cantidad); // Aquí se actualiza la fecha de última reposición dentro del método
    }
        
    // HU4 - Actualizar stock tras venta: valida el stock disponible, descuenta las unidades
    // vendidas y registra la venta en el historial.
    public Venta venderProducto(MaquinaExpendedora m, Producto p, int cantidad)
            throws InsufficientStockException, EntityNotFoundException {
        Stock stock = m.buscarStockProducto(p);
        if (stock == null) {
            throw new EntityNotFoundException(
                    "El producto " + p.getId() + " no está en la máquina " + m.getId());
        }
        // Condición 2 y 3 de HU4: lanza InsufficientStockException si cantidad <= 0 o stock insuficiente.
        stock.decrementar(cantidad);
        Venta nuevaVenta = new Venta(m.getId(), p.getId(), cantidad, LocalDateTime.now());
        ventaDAO.registrar(nuevaVenta);
        return nuevaVenta;
    }

    // HU5 - Detectar productos a reponer: devuelve los Stock que están bajo el umbral mínimo
    // o cuya velocidad de consumo indica agotamiento en 3 días o menos.
    public List<Stock> consultarProductosBajoStock(MaquinaExpendedora m, int umbral) {
        List<Stock> resultado = new ArrayList<>();
        for (Stock s : m.getListaStock()) {
            // Criterio 1: por debajo del umbral estático.
            if (s.isBajoMinimos(umbral)) {
                resultado.add(s);
                continue;
            }
            // Criterio 2: velocidad de consumo indica agotamiento en ≤ 3 días.
            double velocidad = calcularVelocidadConsumo(m, s.getProducto());
            if (velocidad > 0 && s.getCantidadActual() / velocidad <= 3) {
                resultado.add(s);
            }
        }
        return resultado;
    }

    // HU6 - Calcular fecha límite de reposición: estima cuándo se agotará un producto
    // basándose en el historial de ventas y propone una fecha de visita para el operario.
    public LocalDate estimarFechaReposicion(MaquinaExpendedora m, Producto p) {
        Stock s = m.buscarStockProducto(p);
        if (s == null) return null;

        double velocidadConsumo = calcularVelocidadConsumo(m, p);
        if (velocidadConsumo == 0)
            throw new ArithmeticException("No hay datos de consumo para este producto.");

        // Días hasta agotamiento con 1 día de margen de seguridad.
        int diasParaAgotar = (int) (s.getCantidadActual() / velocidadConsumo);
        return LocalDate.now().plusDays(diasParaAgotar).minusDays(1);
    }

    // Utilidad compartida por HU5 y HU6: calcula la velocidad de consumo media (unidades/día)
    // desde la última reposición hasta hoy.
    private double calcularVelocidadConsumo(MaquinaExpendedora m, Producto p) {
        Stock s = m.buscarStockProducto(p);
        if (s == null || s.getFechaUltimaReposicion() == null) return 0;

        LocalDateTime desde = s.getFechaUltimaReposicion().atStartOfDay();
        List<Venta> ventasRecientes = ventaDAO.buscarDesdeFecha(m.getId(), p.getId(), desde);

        int totalUnidades = 0;
        for (Venta v : ventasRecientes) totalUnidades += v.getUnidades();

        long diasTranscurridos = ChronoUnit.DAYS.between(s.getFechaUltimaReposicion(), LocalDate.now());
        if (diasTranscurridos == 0) diasTranscurridos = 1;

        return (double) totalUnidades / diasTranscurridos;
    }

    /*
    //HU3: Venta (alternativa descartada, integrada en venderProducto de HU4)
    */
}
