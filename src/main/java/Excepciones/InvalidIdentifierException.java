package Excepciones;

public class InvalidIdentifierException extends Exception {
    public InvalidIdentifierException(String mensaje) {
        super(mensaje);
    }
}