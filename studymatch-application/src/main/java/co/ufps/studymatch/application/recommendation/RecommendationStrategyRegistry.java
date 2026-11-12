package co.ufps.studymatch.application.recommendation;

import co.ufps.studymatch.recommendation.AcademicLevelStrategy;
import co.ufps.studymatch.recommendation.CareerDiversityStrategy;
import co.ufps.studymatch.recommendation.CompositeRecommendationStrategy;
import co.ufps.studymatch.recommendation.RecommendationStrategy;
import co.ufps.studymatch.recommendation.RecommendationWeights;
import co.ufps.studymatch.recommendation.ScheduleOverlapStrategy;
import co.ufps.studymatch.recommendation.SubjectAffinityStrategy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class RecommendationStrategyRegistry {
    private final List<RecommendationStrategy> baseStrategies;
    private final CompositeRecommendationStrategy composite;
    private final Map<String, RecommendationStrategy> strategies;

    @Inject
    public RecommendationStrategyRegistry(RecommendationWeights weights) {
        this.baseStrategies = List.of(
                new SubjectAffinityStrategy(),
                new ScheduleOverlapStrategy(),
                new CareerDiversityStrategy(),
                new AcademicLevelStrategy()
        );
        this.composite = new CompositeRecommendationStrategy(baseStrategies, weights.asMap());
        Map<String, RecommendationStrategy> available = new LinkedHashMap<>();
        baseStrategies.forEach(strategy -> available.put(strategy.name(), strategy));
        available.put(composite.name(), composite);
        available.put("default", composite);
        this.strategies = Map.copyOf(available);
    }

    public RecommendationStrategy get(String name) {
        String key = name == null || name.isBlank() ? "default" : name.strip().toLowerCase();
        RecommendationStrategy strategy = strategies.get(key);
        if (strategy == null) {
            throw new IllegalArgumentException("Estrategia de recomendacion invalida: " + name
                    + ". Estrategias disponibles: " + String.join(", ", strategies.keySet()));
        }
        return strategy;
    }

    public List<RecommendationStrategy> baseStrategies() {
        return baseStrategies;
    }

    public CompositeRecommendationStrategy composite() {
        return composite;
    }

    public List<RecommendationStrategy> componentsFor(String name) {
        RecommendationStrategy strategy = get(name);
        if (strategy == composite) {
            return baseStrategies;
        }
        return List.of(strategy);
    }

    public Map<String, RecommendationStrategy> all() {
        return strategies;
    }
}
