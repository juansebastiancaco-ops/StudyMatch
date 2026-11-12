package co.ufps.studymatch.application.auth;

import co.ufps.studymatch.ValidationException;

public record LoginCommand(String email, String password) {
    public LoginCommand {
        if (email == null || email.isBlank()) {
            throw new ValidationException("El email es obligatorio");
        }
        if (password == null || password.isBlank()) {
            throw new ValidationException("La contrasena es obligatoria");
        }
    }
}
