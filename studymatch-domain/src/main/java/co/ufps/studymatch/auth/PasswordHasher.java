package co.ufps.studymatch.auth;

/**
 * Puerto para hashing de contrasenas. La implementacion real vive en infraestructura.
 */
public interface PasswordHasher {
    String hash(String password);

    boolean verify(String password, String hash);
}
