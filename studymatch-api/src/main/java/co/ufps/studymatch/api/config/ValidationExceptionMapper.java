package co.ufps.studymatch.api.config;

import co.ufps.studymatch.ValidationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {
    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ValidationException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(ApiError.of(400, "Bad Request", exception.getMessage(), uriInfo.getPath()))
                .build();
    }
}
