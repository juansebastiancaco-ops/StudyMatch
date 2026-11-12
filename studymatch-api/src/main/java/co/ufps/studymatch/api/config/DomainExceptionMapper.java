package co.ufps.studymatch.api.config;

import co.ufps.studymatch.DomainException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class DomainExceptionMapper implements ExceptionMapper<DomainException> {
    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(DomainException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(ApiError.of(400, "Bad Request", exception.getMessage(), uriInfo.getPath()))
                .build();
    }
}
