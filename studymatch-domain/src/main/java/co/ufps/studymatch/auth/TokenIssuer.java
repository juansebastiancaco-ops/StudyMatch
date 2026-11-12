package co.ufps.studymatch.auth;

import co.ufps.studymatch.student.StudentId;

import java.util.Optional;

/**
 * Puerto de autenticacion JWT. La implementacion firma y verifica tokens.
 */
public interface TokenIssuer {
    IssuedToken issue(StudentId studentId, String email, String fullName);

    Optional<TokenClaims> verify(String token);
}
