package co.ufps.studymatch.application.auth;

import co.ufps.studymatch.application.student.StudentDTO;

import java.time.Instant;

public record AuthResponse(
        String token,
        String tokenType,
        Instant expiresAt,
        StudentDTO student
) {
}
