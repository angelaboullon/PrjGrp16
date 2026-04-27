package Excepciones;

public class CapacityExceededException extends Exception {
    public CapacityExceededException(String mensaje) {
        super(mensaje);
    }
}