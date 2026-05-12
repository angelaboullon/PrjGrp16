package Entidades;

import Excepciones.InsufficientStockException;
import Excepciones.FullCapacityException;
import java.time.LocalDate;

public class Stock 
{
	// ========================
	// ATRIBUTOS DE LA CLASE
	// ========================
	private Producto producto;
    private int cantidadActual;
    private int capacidadMax;
    private LocalDate fechaUltimaReposicion;
    private double velocidadConsumo;

    
    
    // ===================
    // GETTERS Y SETTERS
    // ===================
    
    public Producto getProducto() { return producto; }
    public void setProducto(Producto p) { this.producto = p; }
    
    public int getCantidadActual() { return cantidadActual; }
    public void setCantidadActual(int c) { this.cantidadActual = c; }
    
    public int getCapacidadMax() { return capacidadMax; }
    public void setCapacidadMax(int cm) { this.capacidadMax = cm; }
    
    public LocalDate getFechaUltimaReposicion() { return fechaUltimaReposicion; }
    public void setFechaUltimaReposicion(LocalDate f) { this.fechaUltimaReposicion = f; }
    
    public double getVelocidadConsumo() { return velocidadConsumo; }
    public void setVelocidadConsumo(double v) { this.velocidadConsumo = v; }

    
    
    // ==========
    // MÉTODOS
    // ==========
    
    /** incrementar(): incrementa las existencias del producto (proceso de reposición). **/
    public void incrementar(int n) throws FullCapacityException 
    {
        // Validación de desbordamiento de capacidad.
    	if (this.cantidadActual + n > this.capacidadMax)
            throw new FullCapacityException("No cabe tanta cantidad en este muelle.");
        
    	this.cantidadActual += n;
    	
    	// Registro de la estampa de tiempo.
        this.fechaUltimaReposicion = LocalDate.now();
    }
    
    /** decrementar(): reduce las unidades disponibles tras una compra exitosa. **/
    public void decrementar(int cantidad) throws InsufficientStockException 
    {
        // Validación de integridad: no se pueden procesar ventas no positivas.
    	if (cantidad <= 0)
            throw new IllegalArgumentException("La cantidad de venta debe ser positiva.");

    	// Verificación de disponibilidad para evitar stock negativo.
        if (this.cantidadActual - cantidad < 0) 
        {
            throw new InsufficientStockException("Stock insuficiente. Solicitado: " 
                    + cantidad + ", Disponible: " + this.cantidadActual);
        }
        this.cantidadActual -= cantidad;
    }

    /** hayStock(): determina de forma rápida si el muelle contiene al menos una unidad de venta. **/
    public boolean hayStock() { return cantidadActual > 0; }

    /** calcularPorcentajeOcupacion(): calcula el nivel de llenado del muelle en términos porcentuales. **/
    public double calcularPorcentajeOcupacion() { return (double) (cantidadActual * 100) / capacidadMax; }

    /** isBajoMinimos(): compara las existencias actuales con un límite de seguridad definido externamente. **/
    public boolean isBajoMinimos(int umbral) { return cantidadActual < umbral; }

    /** getDiasParaAgotar(): proyecta los días restantes hasta el agotamiento basándose en la velocidad de consumo.
     *  Retorna 999 como valor seguro si la velocidad es 0 (producto estancado) para evitar división por cero. **/
    public int getDiasParaAgotar() 
    {
        if (velocidadConsumo == 0.0) return 999;
        return (int)(cantidadActual / velocidadConsumo);
    }
}