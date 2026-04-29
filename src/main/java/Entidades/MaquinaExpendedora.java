package Entidades;

import java.util.ArrayList;
import java.util.List;

import Excepciones.*;


public class MaquinaExpendedora {
    private String id;
    private String nombre;
    private Localizacion localizacion;
    private Estado estado;
    private List<Stock> listaStock = new ArrayList<>();
    private int capacidad;

    public String getId() { return id; }
    public void setID(String id) throws InvalidIdentifierException {
        if (id == null || !id.matches("M-\\d{3}")) {
            throw new InvalidIdentifierException("El ID debe ser M-XXX.");
        }
        this.id = id;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) throws MalformedNameException {
        if (nombre == null || nombre.trim().length() < 3 || nombre.trim().length() > 50) {
            throw new MalformedNameException("Nombre inválido.");
        }
        this.nombre = nombre;
    }

    public Localizacion getLocalizacion() { return localizacion; }
    public void setLocalizacion(Localizacion localizacion) {
        this.localizacion = localizacion;
    }
    
    public void setEstado(Estado estado) { this.estado = estado; }
    
    public List<Stock> getListaStock() { return listaStock; }
    
    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) throws CapacityOutOfRangeException {
        if (capacidad <= 0 || capacidad > 1000) {
            throw new CapacityOutOfRangeException("Capacidad fuera de rango (1-1000).");
        }
        this.capacidad = capacidad;
    }

    

    // Métodos operativos
    public void addStock(Stock s) {
        this.listaStock.add(s);
    }

    public int calcularEspacioOcupado() {
        int total = 0;
        for (Stock s : listaStock) {
            total += s.getCapacidadMax();
        }
        return total;
    }
    
    public Stock buscarStockProducto(Producto p) {
        for (Stock s : listaStock) {
            if (s.getProducto().getId().equals(p.getId())) return s;
        }
        return null;
    }

    public boolean estaLlena() {
        return calcularEspacioOcupado() >= capacidad;
    }

}

