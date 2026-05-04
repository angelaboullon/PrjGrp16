package Excepciones;

public class ProductAlreadyAssignedException extends Exception {
    public ProductAlreadyAssignedException(String mensaje) {
        super(mensaje);
    }
}