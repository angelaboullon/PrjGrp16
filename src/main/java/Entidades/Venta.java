package Entidades;

import java.time.LocalDateTime;

public class Venta {
    private String idMaquina;
    private String idProducto;
    private int unidades;
    private LocalDateTime fechaVenta;

    public Venta(String idMaquina, String idProducto, int unidades, LocalDateTime fechaVenta) {
        this.idMaquina = idMaquina;
        this.idProducto = idProducto;
        this.unidades = unidades;
        this.fechaVenta = fechaVenta;
    }

    public String getIdMaquina() { return idMaquina; }
    public String getIdProducto() { return idProducto; }
    public int getUnidades() { return unidades; }
    public LocalDateTime getFechaVenta() { return fechaVenta; }

    public void setUnidades(int n) { this.unidades = n; }
    public void setFechaVenta(LocalDateTime fecha) { this.fechaVenta = fecha; }
}