package co.ufps.studymatch;

/**
 * Excepcion base del dominio. No depende de infraestructura ni de frameworks.
 */
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
