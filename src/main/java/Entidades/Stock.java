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
    private double velocidadConsumo; // Unidades/día (Calculada en HU6)

    
    
    // ===================
    // GETTERS Y SETTERS
    // ===================
    
    public Producto getProducto() { return producto; }
    public void setProducto(Producto p) { this.producto = p; }
    
    public int getCantidadActual() { return cantidadActual; }
    public void setCantidadActual(int c) {
        if (c < 0) throw new IllegalArgumentException("El stock no puede ser negativo."); 
        this.cantidadActual = c;
    }
    
    public int getCapacidadMax() { return capacidadMax; }
    public void setCapacidadMax(int cm) { this.capacidadMax = cm; }
    
    public LocalDate getFechaUltimaReposicion() { return fechaUltimaReposicion; }
    public void setFechaUltimaReposicion(LocalDate f) { this.fechaUltimaReposicion = f; }
    
    public double getVelocidadConsumo() { return velocidadConsumo; }
    public void setVelocidadConsumo(double v) { 
        if (v < 0) throw new IllegalArgumentException("La velocidad no puede ser negativa.");
        this.velocidadConsumo = v; 
    }

    
    
    
    // ==========
    // MÉTODOS
    // ==========
    
    /** incrementar(): incrementa las existencias del producto (proceso de reposición). **/
   
    public void incrementar(int n) throws FullCapacityException {
        // Si la suma supera el máximo, lanzamos la excepción que espera el test CP-31
        if (this.cantidadActual + n > this.capacidadMax) {
            throw new FullCapacityException("No cabe tanta cantidad en este muelle.");
        }
        this.cantidadActual += n;
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
    
    /** Calcula cuántos días tardará en agotarse el stock al ritmo actual **/
    public int getDiasParaAgotar() {
        if (this.velocidadConsumo <= 0) return 999; // Evita división por cero
        return (int) (this.cantidadActual / this.velocidadConsumo);
    }
}