package co.ufps.studymatch.application.recommendation;

import co.ufps.studymatch.application.group.StudyGroupSummaryDTO;
import co.ufps.studymatch.application.student.StudentSummaryDTO;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Cache thread-safe alimentada por los workers. Los endpoints pueden recalcular bajo demanda,
 * pero el dashboard puede leer estos valores para evitar trabajo repetido.
 */
@ApplicationScoped
public class RecommendationCache {
    private final ConcurrentHashMap<String, List<StudentSummaryDTO>> studentsByStudent = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<StudyGroupSummaryDTO>> groupsByStudent = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Double> affinityScores = new ConcurrentHashMap<>();
    private final AtomicReference<Instant> lastRefresh = new AtomicReference<>();

    public void putStudentRecommendations(String studentId, List<StudentSummaryDTO> recommendations) {
        studentsByStudent.put(studentId, List.copyOf(recommendations));
    }

    public Optional<List<StudentSummaryDTO>> studentRecommendations(String studentId) {
        return Optional.ofNullable(studentsByStudent.get(studentId));
    }

    public void putGroupRecommendations(String studentId, List<StudyGroupSummaryDTO> recommendations) {
        groupsByStudent.put(studentId, List.copyOf(recommendations));
    }

    public Optional<List<StudyGroupSummaryDTO>> groupRecommendations(String studentId) {
        return Optional.ofNullable(groupsByStudent.get(studentId));
    }

    public void putAffinity(String studentId, String groupId, double score) {
        affinityScores.put(key(studentId, groupId), score);
    }

    public Optional<Double> affinity(String studentId, String groupId) {
        return Optional.ofNullable(affinityScores.get(key(studentId, groupId)));
    }

    public void markRefreshed() {
        lastRefresh.set(Instant.now());
    }

    public Optional<Instant> lastRefresh() {
        return Optional.ofNullable(lastRefresh.get());
    }

    public void clear() {
        studentsByStudent.clear();
        groupsByStudent.clear();
        affinityScores.clear();
        lastRefresh.set(null);
    }

    public int affinitySize() {
        return affinityScores.size();
    }

    private String key(String studentId, String groupId) {
        return studentId + ":" + groupId;
    }
}
