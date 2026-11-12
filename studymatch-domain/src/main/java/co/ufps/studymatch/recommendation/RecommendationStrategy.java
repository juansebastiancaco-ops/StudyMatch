package co.ufps.studymatch.recommendation;

/**
 * Patron Strategy: cada estrategia calcula un puntaje de afinidad de forma aislada.
 */
public interface RecommendationStrategy {
    String name();

    String description();

    RecommendationScore score(RecommendationContext context);
}
