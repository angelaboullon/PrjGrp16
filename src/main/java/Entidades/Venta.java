package Entidades;

import java.time.LocalDateTime;

public class Venta 
{
	// ========================
	// ATRIBUTOS DE LA CLASE
	// ========================
    private String idMaquina;
    private String idProducto;
    private int unidades;
    private LocalDateTime fechaVenta;

    
    
    // ================
 	// CONSTRUCTORES
 	// ================
    public Venta(String idMaquina, String idProducto, int unidades, LocalDateTime fechaVenta) 
    {
        this.idMaquina = idMaquina;
        this.idProducto = idProducto;
        this.unidades = unidades;
        this.fechaVenta = fechaVenta;
    }

    
    
    // ===================
    // GETTERS Y SETTERS
    // ===================
    
    public String getIdMaquina() { return idMaquina; }
    
    public String getIdProducto() { return idProducto; }
    
    public int getUnidades() { return unidades; }
    public void setUnidades(int n) { this.unidades = n; }
    
    public LocalDateTime getFechaVenta() { return fechaVenta; }
    public void setFechaVenta(LocalDateTime fecha) { this.fechaVenta = fecha; }
}