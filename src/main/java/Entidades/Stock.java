package Entidades;

import Excepciones.FullCapacityException;
import Excepciones.InsufficientStockException;
import java.time.LocalDate;

public class Stock 
{
    private Producto producto;
    private int cantidadActual;
    private int capacidadMax;
    private LocalDate fechaUltimaReposicion;
    private int unidadesVendidas;

    // Getters y Setters básicos
    public Producto getProducto() { return producto; }
    public void setProducto(Producto p) { this.producto = p; }
    // HU3 - Consultar stock de una máquina: devuelve las unidades disponibles
    // en ese instante.
    public int getCantidadActual() { return cantidadActual; }
    public void setCantidadActual(int c) { this.cantidadActual = c; }
    public int getCapacidadMax() { return capacidadMax; }
    // HU2 - Asociar productos a máquina: define el límite físico del muelle
    // para ese producto.
    public void setCapacidadMax(int cm) { this.capacidadMax = cm; }
    // HU6 - Calcular fecha límite de reposición: punto de partida temporal para el cálculo
    // de la velocidad.
    public LocalDate getFechaUltimaReposicion() { return fechaUltimaReposicion; }
    public void setFechaUltimaReposicion(LocalDate f) { this.fechaUltimaReposicion = f; }
    public int getUnidadesVendidas() { return unidadesVendidas; }
    public void setUnidadesVendidas(int uv) { this.unidadesVendidas = uv; }

    // Lógica
    public void incrementar(int n) throws FullCapacityException 
    {
        if (this.cantidadActual + n > this.capacidadMax)
            throw new FullCapacityException("No cabe tanta cantidad en este muelle.");
        this.cantidadActual += n;
        this.fechaUltimaReposicion = LocalDate.now();
    }
    
    // HU4 - Actualizar stock tras venta: resta las unidades validando que no se quede en 
    // negativo.
    public void decrementar(int cantidad) throws InsufficientStockException 
    {
        if (cantidad <= 0)
            throw new IllegalArgumentException("La cantidad de venta debe ser positiva.");

        if (this.cantidadActual - cantidad < 0) 
        {
            throw new InsufficientStockException("Stock insuficiente. Solicitado: " 
                    + cantidad + ", Disponible: " + this.cantidadActual);
        }
        this.cantidadActual -= cantidad;
    }

    public void addVenta(int n) throws InsufficientStockException {
        decrementar(n);
        this.unidadesVendidas += n;
    }

    // HU5 - Detectar productos a reponer: comprobación binaria de disponibilidad.
    public boolean hayStock() { return cantidadActual > 0; }

    public double calcularPorcentajeOcupacion() { return (double) (cantidadActual * 100) / capacidadMax; }

    // HU5 - Detectar productos a reponer: compara la cantidad actual con el límite de alerta definido.
    public boolean isBajoMinimos(int umbral) { return cantidadActual < umbral; }
}
