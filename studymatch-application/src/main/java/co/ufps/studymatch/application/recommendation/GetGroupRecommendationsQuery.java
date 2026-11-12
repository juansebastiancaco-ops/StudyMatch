package co.ufps.studymatch.application.recommendation;

import co.ufps.studymatch.application.group.StudyGroupSummaryDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class GetGroupRecommendationsQuery {
    private final RecommendationService recommendationService;

    @Inject
    public GetGroupRecommendationsQuery(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    public List<StudyGroupSummaryDTO> execute(String studentId, String strategy, int limit) {
        return recommendationService.recommendGroups(studentId, strategy, limit);
    }
}
