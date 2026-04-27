package Excepciones;

public class DuplicateIdentifierException extends Exception {
    public DuplicateIdentifierException(String mensaje) {
        super(mensaje);
    }
}