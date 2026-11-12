package co.ufps.studymatch.recommendation;

import co.ufps.studymatch.ValidationException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Pesos configurables para la estrategia compuesta. Los valores por defecto priorizan
 * asignaturas y horario, que son las senales mas fuertes de un match academico.
 */
public record RecommendationWeights(
        double subjectAffinity,
        double scheduleOverlap,
        double careerDiversity,
        double academicLevel
) {
    public RecommendationWeights {
        if (subjectAffinity < 0 || scheduleOverlap < 0 || careerDiversity < 0 || academicLevel < 0) {
            throw new ValidationException("Los pesos de recomendacion no pueden ser negativos");
        }
        if (subjectAffinity + scheduleOverlap + careerDiversity + academicLevel <= 0) {
            throw new ValidationException("La suma de pesos de recomendacion debe ser mayor que cero");
        }
    }

    public static RecommendationWeights defaults() {
        return new RecommendationWeights(0.35, 0.30, 0.20, 0.15);
    }

    public Map<String, Double> asMap() {
        Map<String, Double> weights = new LinkedHashMap<>();
        weights.put(SubjectAffinityStrategy.NAME, subjectAffinity);
        weights.put(ScheduleOverlapStrategy.NAME, scheduleOverlap);
        weights.put(CareerDiversityStrategy.NAME, careerDiversity);
        weights.put(AcademicLevelStrategy.NAME, academicLevel);
        return Map.copyOf(weights);
    }
}
