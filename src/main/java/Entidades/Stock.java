package Entidades;

import Excepciones.FullCapacityException;
import Excepciones.InsufficientStockException;
import java.time.LocalDate;

public class Stock {
    private Producto producto;
    private int cantidadActual;
    private int capacidadMax;
    private LocalDate fechaUltimaReposicion;

    // Getters y Setters básicos
    public Producto getProducto() { return producto; }
    public void setProducto(Producto p) { this.producto = p; }
    public int getCantidadActual() { return cantidadActual; }
    public void setCantidadActual(int c) { this.cantidadActual = c; }
    public int getCapacidadMax() { return capacidadMax; }
    public void setCapacidadMax(int cm) { this.capacidadMax = cm; }
    public LocalDate getFechaUltimaReposicion() { return fechaUltimaReposicion; }
    public void setFechaUltimaReposicion(LocalDate f) { this.fechaUltimaReposicion = f; }

    // Lógica
    public void incrementar(int n) throws FullCapacityException {
        if (this.cantidadActual + n > this.capacidadMax) {
            throw new FullCapacityException("No cabe tanta cantidad en este muelle.");
        }
        this.cantidadActual += n;
        this.fechaUltimaReposicion = LocalDate.now();
    }

    public void decrementar(int n) throws InsufficientStockException {
        if (this.cantidadActual - n < 0) {
            throw new InsufficientStockException("No hay suficiente stock para realizar la venta.");
        }
        this.cantidadActual -= n;
    }

    public boolean hayStock() {
        return cantidadActual > 0;
    }

    public double calcularPorcentajeOcupacion() {
        return (double) (cantidadActual * 100) / capacidadMax;
    }

    public boolean isBajoMinimos(int umbral) {
        return cantidadActual < umbral;
    }
}