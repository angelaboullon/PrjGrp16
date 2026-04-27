package Excepciones;

public class FullCapacityException extends Exception {
    public FullCapacityException(String mensaje) {
        super(mensaje);
    }
}