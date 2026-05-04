package Excepciones;

public class CapacityOutOfRangeException extends Exception {
    public CapacityOutOfRangeException(String mensaje) {
        super(mensaje);
    }
}