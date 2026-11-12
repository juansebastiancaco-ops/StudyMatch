package co.ufps.studymatch;

/**
 * Marca errores de validacion de datos de entrada. La API los traduce a HTTP 400.
 */
public class ValidationException extends DomainException {
    public ValidationException(String message) {
        super(message);
    }
}
