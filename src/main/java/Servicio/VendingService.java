package Servicio;

import Entidades.*;
import DAO.*;
import Excepciones.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    
    // HU3 - Consultar stock de una máquina:
    // consultarStock(m): recupera la lista complea de muelles de una máquina.
    
    
    // HU4 - Actualizar stock tras venta:
    // venderProducto(m, p, cantidad): coordina la resta de unidades y la creación del ticket.
    
    
    // HU5 - Detectar productos a reponer:
    // consultarProductosBajoStock(m, umbral): filtra los productos que necesitan atención
    // inmediata.
    
    
    // HU6 - Calcular fecha límite de reposición: algoritmo que calcula la velocidad de consumo
    // (V=unidades/tiempo) y proyecta el agotamiento. Estima cuándo se agotará un producto basándose 
    // en el historial de ventas y propone una fecha de visita para el operario.
    public LocalDate estimarFechaReposicion (MaquinaExpendedora m, Producto p)
    {
    	// 1. Se busca el objeto Stock asociado al producto 'p' en esa máquina específica 'm'.
    	Stock s = m.buscarStockProducto(p);
    	
    	// 2. Si el producto no está asignado a la máquina (s == null), no se puede calcular 
    	// nada y se retorna null.
    	if (s == null) return null;
    	
    	// 3. Se convierte la fecha de la última reposición a LocalDateTime (inicio del día) para
    	// comparar con las ventas. Esto marca el punto de partida del 'ciclo de consumo' actual.
    	LocalDateTime desde = s.getFechaUltimaReposicion().atStartOfDay();
    	
    	// 4. Se consulta al DAO todas las ventas registradas para este producto.
    	List<Venta> ventasRecientes = ventaDAO.buscarDesdeFecha(m.getId(), p.getId(), desde);
    	
    	// 5. Se inicializa un acumulador para sumar el total de unidades vendidas en este periodo.
    	int totalUnidades = 0;
    	
    	// 6. Se recorre la lista de ventas recuperadas y se suman sus unidades al acumulador.
    	for (Venta v: ventasRecientes) totalUnidades += v.getUnidades();
    	
    	// 7. Se calcula la diferencia de días entre la última vez que se rellenó y el día de hoy.
    	long diasTranscurridos = ChronoUnit.DAYS.between(s.getFechaUltimaReposicion(), LocalDate.now());
    	
    	// 8. Si la reposición fue hoy, el resultado sería 0. Se fuerza a 1 para evitar errores matemáticos
    	// de división por cero.
    	if (diasTranscurridos == 0) diasTranscurridos = 1; 
    	
    	// 9. Se calcula la Velocidad de Consumo: promedio de unidades vendidas por día. Se utiliza (double)
    	// para no perder los decimales en la división.
    	double velocidadConsumo = (double) totalUnidades / diasTranscurridos;
    	
    	// 10. Si no ha habido ninguna venta (velocidad 0), se lanza una excepción porque no hay datos para
    	// predecir.
    	if (velocidadConsumo == 0) throw new ArithmeticException("No hay datos de consumo para este producto.");
    	
    	// 11. Se calcula cuántos días tardará en agotarse el stock actual dividiendo lo que queda por la velocidad
    	// de consumo. El cast a (int) trunca los decimales, dando días completos.
    	int diasParaAgotar = (int)(s.getCantidadActual() / velocidadConsumo);
    	
    	// 12. Se calcula la fecha final: se suman los días de vida que le quedan al stock a la fecha de hoy.
    	// Se resta 1 día (minusDays(1)) como margen de seguridad para que el operario llegue antes del agotamiento total.
    	// Se retorna el día de hoy + días para agotar - 1 día de margen.
    	return LocalDate.now().plusDays(diasParaAgotar).minusDays(1);
    }
    
    /*
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
    }*/
    
    // HU4: Estimación de fechas.
    
    // HU5:  

}