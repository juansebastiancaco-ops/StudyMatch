package co.ufps.studymatch.api.security;

import java.security.Principal;

public record StudentPrincipal(String studentId, String email, String fullName) implements Principal {
    @Override
    public String getName() {
        return studentId;
    }
}
