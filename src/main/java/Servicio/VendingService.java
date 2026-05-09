package Servicio;

import Excepciones.*;
import Entidades.*;
import DAO.*;

import java.time.temporal.ChronoUnit;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * VendingService.java: CAPA DE SERVICIO
 * 
 * Esta clase actúa como el 'Cerebro Logístico' del sistema. Su responsabilidad principal es la implementación
 * de las reglas de negocio.
 * Sigue el principio de inyección de dependencias al recibir los DAOs por constructor, lo que desacopla la
 * lógica de la persistencia de datos.
 **/
public class VendingService 
{
	// ========================
	// ATRIBUTOS DE LA CLASE
	// ========================
	
	// Objeto de Acceso a Datos para la gestión de puntos geográficos. 
	private LocalizacionDAO localizacionDAO;
	
	// Objeto de Acceso a Datos para el catálogo de productos. 
	private ProductoDAO productoDAO;
	
	// Objeto de Acceso a Datos para el inventario de máquinas expendedoras.
	private MaquinaDAO maquinaDAO;
	
	// Objeto de Acceso a Datos para el registro histórico de transacciones.
    private VentaDAO ventaDAO;


    
    // ================
    // CONSTRUCTORES
    // ================
    
    // Constructor sin argumentos.
    public VendingService() {}

    // Constructor principal: vincula el servicio con sus respectivos repositorios de datos.
    public VendingService(MaquinaDAO mDao, LocalizacionDAO lDao, ProductoDAO pDao, VentaDAO vDao) 
    {
        this.maquinaDAO = mDao;
        this.localizacionDAO = lDao;
        this.productoDAO = pDao;
        this.ventaDAO = vDao;
    }

    
    
    // =======================================
    // HU1 - CARGAR MÁQUINAS EN EL SISTEMA
    // =======================================
    
    /**
     * registrarLocalizacion().
     * 
     * Este método se encarga de registrar un punto geográfico único en el sistema.
     * Antes de insertar, verifica mediante las coordenadas GPS (latitud/longitud) que no exista 
     * ya un registro idéntico para evitar redundancia de datos.
     **/
    public void registrarLocalizacion(Localizacion l) throws DuplicateLocationException 
    {
        if (localizacionDAO.buscarPorCoordenadas(l.getLatitud(), l.getLongitud()) != null) 
        {
            throw new DuplicateLocationException("Ya existe una localización en estas coordenadas.");
        }
        localizacionDAO.insertar(l);
    }

    /**
     * darAltaMaquina()
     * 
     * Este método se encarga de llevar a cabo el proceso integral de alta de una máquina expendedora.
     * 1. Asegura que la localización esté registrada en el sistema.
     * 2. Verifica que la localización física esté vacía; es decir, sin otra máquina instalada.
     * 3. Valida la unicidad del identificador (M-XXX) y del nombre comercial.
     **/
    public void darAltaMaquina(MaquinaExpendedora m, Localizacion l) throws Exception 
    {
        try 
        {
        	// Se intenta el registro por si el parámetro l es una ubicación nueva.
            registrarLocalizacion(l);
        } catch (DuplicateLocationException e) 
        {
            // Si la localización l ya existía, el catch captura la excepción y permite continuar:
        	// la ubicación es válida para ser vinculada a la máquina.
        }
        
        // Validación de exclusividad: una localización física solamente puede albergar una máquina
        // a la vez.
        for (MaquinaExpendedora maq : maquinaDAO.listarTodas()) 
        {
            if (maq.getLocalizacion().getLatitud() == l.getLatitud() && 
                maq.getLocalizacion().getLongitud() == l.getLongitud()) 
            {
                throw new LocationOccupiedException("Esta localización ya tiene una máquina asignada.");
            }
        }

        // Validación de identificadores únicos.
        if (maquinaDAO.buscarPorId(m.getId()) != null) 
        {
            throw new DuplicateIdentifierException("ID de máquina repetido.");
        }
        
        // Vinculación final de la entidad Máquina con su Localización.
        m.setLocalizacion(l);
        maquinaDAO.insertar(m);
    }
    
    
    // ========================================
    // HU2 - ASOCIAR PRODUCTOS A UNA MÁQUINA
    // ========================================
    public void darAltaProducto(Producto p) throws Exception 
    {
        if (productoDAO.buscarPorId(p.getId()) != null) throw new DuplicateIdentifierException("ID de producto repetido.");
        if (productoDAO.buscarPorNombre(p.getNombre()) != null) throw new DuplicateNameException("Nombre de producto repetido.");
        productoDAO.insertar(p);
    }

    /**
     * asignarProductoMaquina()
     * 
     * Este método asigna un producto del catálogo a una máquina específica creando un 'muelle' (Stock).
     * En él se controla la capacidad física: la suma de los límites de cada producto (cupoMax) no puede exceder la capacidad
     * total de carga de la máquina expendedora.
     **/
    public void asignarProductoMaquina(MaquinaExpendedora m, Producto p, int cupoMax) throws Exception 
    {
        // Verificación de integridad: ambos elementos deben existir previamente en el sistema.
        if (maquinaDAO.buscarPorId(m.getId()) == null || productoDAO.buscarPorId(p.getId()) == null) 
        {
            throw new EntityNotFoundException("La máquina o el producto no existen.");
        }

        // Se evita duplicar el mismo producto en distintos muelles de la misma máquina.
        if (m.buscarStockProducto(p) != null) 
        {
            throw new ProductAlreadyAssignedException("El producto ya está en esta máquina.");
        }
       
        // Regla de volumen: se controla que no se reserve más espacio del que la máquina permite físicamente.
        if (m.calcularEspacioOcupado() + cupoMax > m.getCapacidad()) 
        {
            throw new CapacityExceededException("No hay espacio suficiente en la máquina para ese cupo.");
        }

        // Creación del objeto Stock (vínculo Producto <-> Máquina con sus propios atributos de cantidad).
        Stock nuevoStock = new Stock();
        nuevoStock.setProducto(p);
        nuevoStock.setCapacidadMax(cupoMax);
        nuevoStock.setCantidadActual(0);	// El muelle nace configurado pero vacío.
        m.addStock(nuevoStock);
    }

    
    /**
     * reponerStock()
     * 
     * Este método se encarga del proceso de reposición de mercancía.
     * Actualiza las unidades y registra la fecha de la operación para el cálculo de velocidad.
     **/
    public void reponerStock(MaquinaExpendedora m, Producto p, int cantidad) throws Exception 
    {
        Stock s = m.buscarStockProducto(p);
        if (s == null) throw new EntityNotFoundException("El producto no está asignado a esta máquina.");
        
        // El método incrementar() de la entidad Stock gestiona la fechaUltimaReposicion automáticamente.
        s.incrementar(cantidad); 
    }

    
    // ========================================
    // HU3 - CONSULTAR STOCK DE UNA MÁQUINA
    // ========================================
    
    /**
     * consultarStock()
     * 
     * 
     **/
    public List<Stock> consultarStock(String id) throws EntityNotFoundException {
        MaquinaExpendedora m = maquinaDAO.buscarPorId(id);
        if (m == null) {
            throw new EntityNotFoundException("La máquina no ha sido encontrada.");
        }
        return m.getListaStock();
    }

    
    // ========================================
    // HU4 - ACTUALIZAR STOCK TRAS VENTA
    // ========================================
    
    /**
     * venderProducto()
     * 
     * Este método se encarga de ejecutar una venta, reduciendo el stock disponible y generando un 
     * registro histórico. 
     * Resulta una función fundamental para que el algoritmo de HU6 funcione.
     **/
    // INSERTAR EL MÉTODO AQUÍ


    
    // ========================================
    // HU5 - DETECTAR PRODUCTOS A REPONER
    // ========================================
    
    /**
     * consultarProductosBajoStock()
     * 
     * Este método identifica y filtra los productos de una máquina que requieren atención inmediata.
     * Actúa como un monitor de alertas preventivas. Su objetivo es generar una 'lista crítica' de
     * aquellos muelles de carga cuya cantidad actual ha caído por debajo de un límite de seguridad 
     * (umbral), permitiendo al gestor priorizar las rutas de reposición antes de que se produzca 
     * una rotura de stock.
     **/
    public List<Stock> consultarProductosBajoStock(MaquinaExpendedora m, int umbral)
    {
    	List<Stock> critica = new ArrayList<>();
    	
    	// Se recorre la lista completa de existencias (muelles) de la máquina proporcionada.
    	for (Stock s : m.getListaStock())
    	{
    		// Se invoca la lógica interna de la entidad Stock para evaluar si la cantidad actual es
    		// inferior al umbral de alerta definido. 
    		// Si el producto está 'bajo mínimos', se añade a la lista de resultados.
    		if (s.isBajoMinimos(umbral)) critica.add(s);
    	}
    	return critica;
    }


    
    // ===========================================
    // HU6 - CALCULAR FECHA LÍMITE DE REPOSICIÓN
    // ===========================================
    
    /**
     * estimarFechaReposicion()
     * 
     * Este método se encarga de calcular la fecha estimada de agotamiento de un producto.
     * 
     * Lógica del algoritmo:
     * 1. Obtiene las ventas realizadas desde la última vez que el operario llenó la máquina.
     * 2. Calcula la velocidad de consumo, V = unidadesVendidas/diasTranscurridos
     * 3. Proyecta cuánto tiempo durará el stock actual basándose en esa velocidad.
     * 4. Propone la visita del operario un día antes (margen de seguridad).
     **/
    public LocalDate estimarFechaReposicion (MaquinaExpendedora m, Producto p)
    {
    	Stock s = m.buscarStockProducto(p);
    	if (s == null) return null;
    	
    	// Punto de corte temporal: se ignoran ventas anteriores a la última carga de stock.
    	LocalDateTime desde = s.getFechaUltimaReposicion().atStartOfDay();
    	
    	// Se recupera el historial de ventas relevante para este ciclo de carga.
    	List<Venta> ventasRecientes = ventaDAO.buscarDesdeFecha(m.getId(), p.getId(), desde);
    	
    	int totalUnidades = 0;
    	for (Venta v: ventasRecientes) totalUnidades += v.getUnidades();
    	
    	// Cálculo del tiempo transcurrido (mínimo 1 día para evitar división por cero en cargas
    	// recientes).
    	long diasTranscurridos = ChronoUnit.DAYS.between(s.getFechaUltimaReposicion(), LocalDate.now());
    	if (diasTranscurridos == 0) diasTranscurridos = 1; 
    	
    	//Se determina el ritmo de salida del producto.
    	double velocidadConsumo = (double) totalUnidades / diasTranscurridos;
    	
    	//Si el producto no tiene rotación (velocidad 0), no se puede realizar una proyección matemática.
    	if (velocidadConsumo == 0) throw new ArithmeticException("No hay datos de consumo para este producto.");
    	
    	// Estimación de días de vida restantes para el stock actual.
    	int diasParaAgotar = (int)(s.getCantidadActual() / velocidadConsumo);
    	
    	// Retorno: fecha actual + vida estimada - 1 día (margen de prevención de rotura de stock).
    	return LocalDate.now().plusDays(diasParaAgotar).minusDays(1);
    } 
}