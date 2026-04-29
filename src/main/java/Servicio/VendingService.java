package Servicio;

import Entidades.*;
import DAO.*;
import Excepciones.*;

import java.time.LocalDateTime;
import java.util.List;

public class VendingService {
    private MaquinaDAO maquinaDAO;
    private LocalizacionDAO localizacionDAO;
    private ProductoDAO productoDAO;
    private VentaDAO ventaDAO;

    // Constructor: Solo necesitamos los DAOs de máquinas y localizaciones para HU1
    public VendingService(MaquinaDAO mDao, LocalizacionDAO lDao, ProductoDAO pDao, VentaDAO vDao) {
        this.maquinaDAO = mDao;
        this.localizacionDAO = lDao;
        this.productoDAO = pDao;
        this.ventaDAO = vDao;
    }

    // HU1: Registro de una ubicación en el sistema
    public void registrarLocalizacion(Localizacion l) throws DuplicateLocationException {
        if (localizacionDAO.buscarPorCoordenadas(l.getLatitud(), l.getLongitud()) != null) {
            throw new DuplicateLocationException("Ya existe una localización en estas coordenadas.");
        }
        localizacionDAO.insertar(l);
    }

    // HU1: Alta de una máquina asociada a una localización
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
    
    // Gestión de Catálogo
    public void darAltaProducto(Producto p) throws Exception {
        if (productoDAO.buscarPorId(p.getId()) != null) throw new DuplicateIdentifierException("ID de producto repetido.");
        if (productoDAO.buscarPorNombre(p.getNombre()) != null) throw new DuplicateNameException("Nombre de producto repetido.");
        productoDAO.insertar(p);
    }

    // HU2: Configuración de Inventario
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
    
    //HU3: Venta
    public Venta registrarVenta(MaquinaExpendedora m, Producto p, int cant) 
            throws InsufficientStockException, EntityNotFoundException {
    	//Buscamos el registro de stock del producto en esa máquina específica.
        Stock stock = m.buscarStockProducto(p);
        
        // 2. Si el producto no existe en la máquina, lanzamos excepción.
        if (stock == null) {
            throw new EntityNotFoundException("El producto " + p.getId() + " no existe en la máquina " + m.getId());
        }
        
        // 3. Intentamos reducir el stock. El método 'decrementar' validará si hay suficiente.
        // Si la cantidad solicitada supera el stock, 'decrementar' lanzará InsufficientStockException.
        stock.decrementar(cant);
        
        // 4. Creamos el objeto Venta usando tu nuevo constructor basado en IDs.
        // Usamos LocalDateTime.now() para registrar el momento exacto.
        Venta nuevaVenta = new Venta(m.getId(), p.getId(), cant, LocalDateTime.now());
        
        // 5. Persistimos la venta en el historial a través del DAO.
        this.ventaDAO.registrar(nuevaVenta);
    }

}