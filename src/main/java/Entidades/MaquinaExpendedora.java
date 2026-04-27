package Entidades;

import Excepciones.*;


public class MaquinaExpendedora {
    private String id;
    private String nombre;
    private Localizacion localizacion;
    private Estado estado;

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
}

