package Excepciones;

public class InvalidPriceException extends Exception {
    public InvalidPriceException(String mensaje) {
        super(mensaje);
    }
}