package Excepciones;

public class MalformedNameException extends Exception {
    public MalformedNameException(String mensaje) {
        super(mensaje);
    }
}