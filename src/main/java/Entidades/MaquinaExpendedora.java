package Entidades;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import Excepciones.*;


public class MaquinaExpendedora 
{
    private String id;
    private String nombre;
    private String modelo;
    private int capacidad;
    private Estado estado;
    private Localizacion localizacion;
    private List<Stock> listaStock = new ArrayList<>();
    private LocalDate fechaInstalacion;
    
    public String getId() { return id; }
    public void setID(String id) throws InvalidIdentifierException 
    {
        if (id == null || !id.matches("M-\\d{3}")) 
            throw new InvalidIdentifierException("El ID debe ser M-XXX.");
        this.id = id;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) throws MalformedNameException 
    {
        if (nombre == null || nombre.trim().length() < 3 || nombre.trim().length() > 50) 
            throw new MalformedNameException("Nombre inválido.");
        this.nombre = nombre;
    }

    public String getModeo() { return modelo; }
    public void setModelo(String modelo) throws InvalidModelFormatException
    {
    	if (modelo == null || !modelo.startsWith("VMM-"))
    		throw new InvalidModelFormatException("El modelo debe empezar por VMM-.");
    	this.modelo = modelo;
    }
    
    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) throws CapacityOutOfRangeException 
    {
        if (capacidad <= 0 || capacidad > 1000)
            throw new CapacityOutOfRangeException("Capacidad fuera de rango (1-1000).");
        this.capacidad = capacidad;
    }
    
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) 
    { 
    	if (estado == null) throw new IllegalArgumentException("El estado no puede ser nulo.");
    	this.estado = estado; 
    }
    
    public Localizacion getLocalizacion() { return localizacion; }
    public void setLocalizacion(Localizacion localizacion) 
    {
    	if (localizacion == null) throw new IllegalArgumentException("La localización no puede ser nula.");
        this.localizacion = localizacion;
    }
    
    // HU3 - Consultar stock de una máquina: acceso directo a la colección de objetos Stock.
    public List<Stock> getListaStock() { return listaStock; }
    
    public LocalDate getFechaInstalacion() { return fechaInstalacion; }
    public void setFechaInstalacion(LocalDate fecha) { this.fechaInstalacion = fecha; }
    

    // Métodos operativos
    public void addStock(Stock s) { this.listaStock.add(s); }

    // HU2 - Asociar productos a máquina: suma el espacio reservado actual para permitir
    // o denegar la asignación de nuevos productos.
    public int calcularEspacioOcupado() 
    {
        int total = 0;
        for (Stock s : listaStock) { total += s.getCapacidadMax(); }
        return total;
    }
    
    public Stock buscarStockProducto(Producto p) 
    {
        for (Stock s : listaStock) 
        {
            if (s.getProducto().getId().equals(p.getId())) return s;
        }
        return null;
    }

    public boolean estaLlena() { return calcularEspacioOcupado() >= capacidad; }

}

