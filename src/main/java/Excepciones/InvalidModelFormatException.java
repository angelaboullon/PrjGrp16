package Excepciones;

public class InvalidModelFormatException extends Exception {
    public InvalidModelFormatException(String mensaje) {
        super(mensaje);
    }
}