package Excepciones;

public class DuplicateLocationException extends Exception {
    public DuplicateLocationException(String mensaje) {
        super(mensaje);
    }
}