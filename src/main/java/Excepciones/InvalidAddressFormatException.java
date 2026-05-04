package Excepciones;
public class InvalidAddressFormatException extends Exception {
    public InvalidAddressFormatException(String mensaje) {
        super(mensaje);
    }
}