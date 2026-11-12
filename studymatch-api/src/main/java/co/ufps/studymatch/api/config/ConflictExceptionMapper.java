package co.ufps.studymatch.api.config;

import co.ufps.studymatch.ConflictException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ConflictExceptionMapper implements ExceptionMapper<ConflictException> {
    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ConflictException exception) {
        return Response.status(Response.Status.CONFLICT)
                .entity(ApiError.of(409, "Conflict", exception.getMessage(), uriInfo.getPath()))
                .build();
    }
}
