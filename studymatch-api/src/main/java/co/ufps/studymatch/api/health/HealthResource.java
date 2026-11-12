package co.ufps.studymatch.api.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.time.Instant;

@Path("/api/v1/health")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {
    private static final Instant STARTED_AT = Instant.now();

    @GET
    public HealthDTO health() {
        return new HealthDTO("UP", "studymatch-api", "1.0.0", STARTED_AT, Instant.now());
    }

    public record HealthDTO(
            String status,
            String service,
            String version,
            Instant startedAt,
            Instant timestamp
    ) {
    }
}
