package Excepciones;

public class EntityNotFoundException extends Exception {
    public EntityNotFoundException(String mensaje) {
        super(mensaje);
    }
}