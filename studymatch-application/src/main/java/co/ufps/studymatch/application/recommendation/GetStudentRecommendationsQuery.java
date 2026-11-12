package co.ufps.studymatch.application.recommendation;

import co.ufps.studymatch.application.student.StudentSummaryDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class GetStudentRecommendationsQuery {
    private final RecommendationService recommendationService;

    @Inject
    public GetStudentRecommendationsQuery(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    public List<StudentSummaryDTO> execute(String studentId, String strategy, int limit) {
        return recommendationService.recommendStudents(studentId, strategy, limit);
    }
}
