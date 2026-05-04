package Excepciones;

public class InsufficientStockException extends Exception {
    public InsufficientStockException(String mensaje) {
        super(mensaje);
    }
}