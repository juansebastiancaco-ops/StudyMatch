package co.ufps.studymatch.api.security;

import jakarta.ws.rs.core.SecurityContext;

import java.security.Principal;

public class StudentSecurityContext implements SecurityContext {
    private final StudentPrincipal principal;

    public StudentSecurityContext(StudentPrincipal principal) {
        this.principal = principal;
    }

    @Override
    public Principal getUserPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public boolean isSecure() {
        return true;
    }

    @Override
    public String getAuthenticationScheme() {
        return "Bearer";
    }
}
