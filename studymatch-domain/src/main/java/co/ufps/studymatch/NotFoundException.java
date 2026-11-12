package co.ufps.studymatch;

/**
 * Marca recursos inexistentes. La API las traduce a HTTP 404.
 */
public class NotFoundException extends DomainException {
    public NotFoundException(String message) {
        super(message);
    }
}
