package co.ufps.studymatch.api.subject;

import co.ufps.studymatch.application.subject.SearchSubjectsQuery;
import co.ufps.studymatch.application.subject.SubjectDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/v1/subjects")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class SubjectResource {
    private final SearchSubjectsQuery searchSubjectsQuery;

    @Inject
    public SubjectResource(SearchSubjectsQuery searchSubjectsQuery) {
        this.searchSubjectsQuery = searchSubjectsQuery;
    }

    @GET
    public List<SubjectDTO> list(@QueryParam("q") String q) {
        return searchSubjectsQuery.execute(q);
    }
}
