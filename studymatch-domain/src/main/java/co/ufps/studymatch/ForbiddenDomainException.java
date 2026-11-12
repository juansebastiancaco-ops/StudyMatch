package co.ufps.studymatch;

/**
 * Marca operaciones no permitidas para el actor actual. La API las traduce a HTTP 403.
 */
public class ForbiddenDomainException extends DomainException {
    public ForbiddenDomainException(String message) {
        super(message);
    }
}
