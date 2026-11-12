package co.ufps.studymatch.api.security;

import co.ufps.studymatch.api.config.ApiError;
import co.ufps.studymatch.auth.TokenClaims;
import co.ufps.studymatch.auth.TokenIssuer;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.util.Optional;
import java.util.Set;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthenticationFilter implements ContainerRequestFilter {
    private static final Set<String> PUBLIC_ROUTES = Set.of(
            "POST api/v1/auth/register",
            "POST api/v1/auth/login",
            "GET api/v1/subjects",
            "GET api/v1/recommendations/strategies",
            "GET api/v1/meta/options",
            "GET api/v1/health"
    );

    @Inject
    TokenIssuer tokenIssuer;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            return;
        }
        String path = normalize(requestContext.getUriInfo().getPath());
        String route = requestContext.getMethod().toUpperCase() + " " + path;
        if (PUBLIC_ROUTES.contains(route)) {
            return;
        }

        String authorization = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            abort(requestContext, "Token JWT faltante");
            return;
        }
        Optional<TokenClaims> claims = tokenIssuer.verify(authorization.substring(7).strip());
        if (claims.isEmpty()) {
            abort(requestContext, "Token JWT invalido o expirado");
            return;
        }
        TokenClaims token = claims.get();
        StudentPrincipal principal = new StudentPrincipal(
                token.studentId().value(),
                token.email(),
                token.fullName()
        );
        requestContext.setSecurityContext(new StudentSecurityContext(principal));
    }

    private void abort(ContainerRequestContext context, String message) {
        context.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .type(MediaType.APPLICATION_JSON)
                .entity(ApiError.of(401, "Unauthorized", message, context.getUriInfo().getPath()))
                .build());
    }

    private String normalize(String path) {
        String normalized = path == null ? "" : path;
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
