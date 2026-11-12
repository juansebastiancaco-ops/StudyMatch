package co.ufps.studymatch.api.recommendation;

import co.ufps.studymatch.api.security.CurrentStudent;
import co.ufps.studymatch.application.group.StudyGroupSummaryDTO;
import co.ufps.studymatch.application.recommendation.GetGroupRecommendationsQuery;
import co.ufps.studymatch.application.recommendation.GetStudentRecommendationsQuery;
import co.ufps.studymatch.application.recommendation.ListStrategiesQuery;
import co.ufps.studymatch.application.recommendation.RecommendationCache;
import co.ufps.studymatch.application.recommendation.StrategyInfoDTO;
import co.ufps.studymatch.application.student.StudentSummaryDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.time.Instant;
import java.util.List;

@Path("/api/v1/recommendations")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class RecommendationResource {
    private final GetStudentRecommendationsQuery getStudentRecommendationsQuery;
    private final GetGroupRecommendationsQuery getGroupRecommendationsQuery;
    private final ListStrategiesQuery listStrategiesQuery;
    private final RecommendationCache recommendationCache;

    @Inject
    public RecommendationResource(GetStudentRecommendationsQuery getStudentRecommendationsQuery,
                                  GetGroupRecommendationsQuery getGroupRecommendationsQuery,
                                  ListStrategiesQuery listStrategiesQuery,
                                  RecommendationCache recommendationCache) {
        this.getStudentRecommendationsQuery = getStudentRecommendationsQuery;
        this.getGroupRecommendationsQuery = getGroupRecommendationsQuery;
        this.listStrategiesQuery = listStrategiesQuery;
        this.recommendationCache = recommendationCache;
    }

    @GET
    @Path("/students")
    public List<StudentSummaryDTO> students(@Context SecurityContext securityContext,
                                            @QueryParam("studentId") String studentId,
                                            @QueryParam("strategy") String strategy,
                                            @QueryParam("limit") int limit) {
        return getStudentRecommendationsQuery.execute(resolveStudentId(securityContext, studentId),
                strategy, limit);
    }

    @GET
    @Path("/groups")
    public List<StudyGroupSummaryDTO> groups(@Context SecurityContext securityContext,
                                             @QueryParam("studentId") String studentId,
                                             @QueryParam("strategy") String strategy,
                                             @QueryParam("limit") int limit) {
        return getGroupRecommendationsQuery.execute(resolveStudentId(securityContext, studentId),
                strategy, limit);
    }

    @GET
    @Path("/strategies")
    public List<StrategyInfoDTO> strategies() {
        return listStrategiesQuery.execute();
    }

    @GET
    @Path("/cache")
    public CacheStatusDTO cacheStatus() {
        return new CacheStatusDTO(recommendationCache.lastRefresh().orElse(null),
                recommendationCache.affinitySize());
    }

    private String resolveStudentId(SecurityContext securityContext, String requestedStudentId) {
        String currentStudentId = CurrentStudent.id(securityContext);
        if (requestedStudentId != null && !requestedStudentId.isBlank()) {
            CurrentStudent.requireSelf(securityContext, requestedStudentId);
            return requestedStudentId;
        }
        return currentStudentId;
    }

    public record CacheStatusDTO(Instant lastRefresh, int affinityEntries) {
    }
}
