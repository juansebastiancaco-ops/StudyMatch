package co.ufps.studymatch.api.security;

import co.ufps.studymatch.application.common.ForbiddenOperationException;
import co.ufps.studymatch.application.common.UnauthorizedException;
import jakarta.ws.rs.core.SecurityContext;

public final class CurrentStudent {
    private CurrentStudent() {
    }

    public static String id(SecurityContext context) {
        if (context == null || context.getUserPrincipal() == null) {
            throw new UnauthorizedException("Debes iniciar sesion");
        }
        return context.getUserPrincipal().getName();
    }

    public static void requireSelf(SecurityContext context, String studentId) {
        if (!id(context).equals(studentId)) {
            throw new ForbiddenOperationException("No puedes acceder a recursos de otro estudiante");
        }
    }
}
