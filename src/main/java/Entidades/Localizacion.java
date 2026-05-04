package Entidades;

import Excepciones.*;

public class Localizacion {
    private String direccion;
    private double latitud;
    private double longitud;

    public String getDireccion() { return direccion; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }

    public void setDireccion(String direccion) throws InvalidAddressFormatException {
        if (direccion == null || direccion.trim().length() < 5 || direccion.trim().length() > 100) {
            throw new InvalidAddressFormatException("La dirección debe tener entre 5 y 100 caracteres.");
        }
        this.direccion = direccion;
    }

    public void setLatitud(double latitud) throws CoordinateOutOfRangeException {
        if (latitud < -90.0 || latitud > 90.0) {
            throw new CoordinateOutOfRangeException("Latitud fuera de rango [-90, 90].");
        }
        this.latitud = latitud;
    }

    public void setLongitud(double longitud) throws CoordinateOutOfRangeException {
        if (longitud < -180.0 || longitud > 180.0) {
            throw new CoordinateOutOfRangeException("Longitud fuera de rango [-180, 180].");
        }
        this.longitud = longitud;
    }
}