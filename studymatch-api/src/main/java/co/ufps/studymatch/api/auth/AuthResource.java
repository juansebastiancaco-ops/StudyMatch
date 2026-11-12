package co.ufps.studymatch.api.auth;

import co.ufps.studymatch.application.auth.AuthResponse;
import co.ufps.studymatch.application.auth.LoginCommand;
import co.ufps.studymatch.application.auth.LoginHandler;
import co.ufps.studymatch.application.auth.RegisterStudentCommand;
import co.ufps.studymatch.application.auth.RegisterStudentHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Endpoints publicos de registro e inicio de sesion.
 */
@Path("/api/v1/auth")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    private final RegisterStudentHandler registerStudentHandler;
    private final LoginHandler loginHandler;

    @Inject
    public AuthResource(RegisterStudentHandler registerStudentHandler, LoginHandler loginHandler) {
        this.registerStudentHandler = registerStudentHandler;
        this.loginHandler = loginHandler;
    }

    @POST
    @Path("/register")
    public Response register(RegisterStudentCommand command) {
        AuthResponse response = registerStudentHandler.execute(command);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @POST
    @Path("/login")
    public AuthResponse login(LoginCommand command) {
        return loginHandler.execute(command);
    }
}
