package co.ufps.studymatch.recommendation;

import co.ufps.studymatch.ValidationException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Combina varias estrategias con pesos. No usa if/else para elegir estrategias:
 * recibe la lista y consulta cada una por su nombre.
 */
public final class CompositeRecommendationStrategy implements RecommendationStrategy {
    public static final String NAME = "composite";

    private final List<RecommendationStrategy> strategies;
    private final Map<String, Double> weights;

    public CompositeRecommendationStrategy(List<RecommendationStrategy> strategies, Map<String, Double> weights) {
        if (strategies == null || strategies.isEmpty()) {
            throw new ValidationException("La estrategia compuesta requiere al menos una estrategia");
        }
        if (weights == null || weights.isEmpty()) {
            throw new ValidationException("La estrategia compuesta requiere pesos");
        }
        this.strategies = List.copyOf(strategies);
        this.weights = new LinkedHashMap<>(weights);
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String description() {
        return "Promedio ponderado de asignaturas, horarios, diversidad y nivel academico";
    }

    @Override
    public RecommendationScore score(RecommendationContext context) {
        double weightedSum = 0;
        double totalWeight = 0;
        for (RecommendationStrategy strategy : strategies) {
            double weight = weights.getOrDefault(strategy.name(), 0.0);
            if (weight <= 0) {
                continue;
            }
            weightedSum += strategy.score(context).value() * weight;
            totalWeight += weight;
        }
        if (totalWeight == 0) {
            return RecommendationScore.zero();
        }
        return RecommendationScore.of(weightedSum / totalWeight);
    }

    public List<RecommendationStrategy> getStrategies() {
        return strategies;
    }

    public Map<String, Double> getWeights() {
        return Map.copyOf(weights);
    }
}
