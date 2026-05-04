package Excepciones;

public class LocationOccupiedException extends Exception {
    public LocationOccupiedException(String mensaje) {
        super(mensaje);
    }
}