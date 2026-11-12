package co.ufps.studymatch.api.config;

import co.ufps.studymatch.application.common.ForbiddenOperationException;
import co.ufps.studymatch.application.common.UnauthorizedException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class ApplicationExceptionMapper implements ExceptionMapper<RuntimeException> {
    private static final Logger LOGGER = Logger.getLogger(ApplicationExceptionMapper.class.getName());

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(RuntimeException exception) {
        if (exception instanceof WebApplicationException webApplicationException) {
            return webApplicationException.getResponse();
        }
        if (exception instanceof UnauthorizedException) {
            return error(401, "Unauthorized", exception.getMessage());
        }
        if (exception instanceof ForbiddenOperationException) {
            return error(403, "Forbidden", exception.getMessage());
        }
        if (exception instanceof IllegalArgumentException) {
            return error(400, "Bad Request", exception.getMessage());
        }
        LOGGER.log(Level.SEVERE, "Error no controlado", exception);
        return error(500, "Internal Server Error", "Ocurrio un error inesperado");
    }

    private Response error(int status, String error, String message) {
        return Response.status(status)
                .entity(ApiError.of(status, error, message, uriInfo.getPath()))
                .build();
    }
}
