package co.ufps.studymatch;

/**
 * Marca conflictos con reglas o estados del negocio. La API los traduce a HTTP 409.
 */
public class ConflictException extends DomainException {
    public ConflictException(String message) {
        super(message);
    }
}
