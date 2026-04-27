package Excepciones;

public class DuplicateNameException extends Exception {
    public DuplicateNameException(String mensaje) {
        super(mensaje);
    }
}