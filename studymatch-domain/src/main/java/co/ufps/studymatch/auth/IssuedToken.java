package co.ufps.studymatch.auth;

import java.time.Instant;

public record IssuedToken(String token, Instant expiresAt) {
    public IssuedToken {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("El token no puede estar vacio");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("La fecha de expiracion es obligatoria");
        }
    }
}
