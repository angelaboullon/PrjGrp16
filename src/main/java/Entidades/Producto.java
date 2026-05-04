package Entidades;

import Excepciones.*;
import java.time.LocalDate;

public class Producto {
    private String id;
    private String nombre;
    private String categoria;
    private double precio;
    private LocalDate fechaAlta;

    // Getters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public double getPrecio() { return precio; }
    public LocalDate getFechaAlta() { return fechaAlta; }

    // Setters con validaciones
    public void setId(String id) throws InvalidIdentifierException {
        if (id == null || !id.matches("P-\\d{3}")) {
            throw new InvalidIdentifierException("El ID de producto debe seguir el patrón P-XXX.");
        }
        this.id = id;
    }

    public void setNombre(String nombre) throws MalformedNameException {
        if (nombre == null || nombre.trim().length() < 3 || nombre.trim().length() > 50) {
            throw new MalformedNameException("El nombre del producto debe tener entre 3 y 50 caracteres.");
        }
        this.nombre = nombre;
    }

    public void setCategoria(String categoria) {
        if (categoria == null || categoria.trim().length() < 3 || categoria.trim().length() > 50) {
            throw new IllegalArgumentException("Categoría inválida.");
        }
        this.categoria = categoria;
    }

    public void setPrecio(double precio) throws InvalidPriceException {
        if (precio <= 0.0 || precio > 1000.0) {
            throw new InvalidPriceException("El precio debe estar entre 0.0 y 1000.0.");
        }
        this.precio = precio;
    }

    public void setFechaAlta(LocalDate fechaAlta) throws FutureDateException {
        if (fechaAlta != null && fechaAlta.isAfter(LocalDate.now())) {
            throw new FutureDateException("La fecha de alta no puede ser futura.");
        }
        this.fechaAlta = fechaAlta;
    }
}