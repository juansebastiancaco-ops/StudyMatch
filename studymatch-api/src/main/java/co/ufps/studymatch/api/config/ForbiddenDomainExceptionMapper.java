package co.ufps.studymatch.api.config;

import co.ufps.studymatch.ForbiddenDomainException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ForbiddenDomainExceptionMapper implements ExceptionMapper<ForbiddenDomainException> {
    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ForbiddenDomainException exception) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(ApiError.of(403, "Forbidden", exception.getMessage(), uriInfo.getPath()))
                .build();
    }
}
