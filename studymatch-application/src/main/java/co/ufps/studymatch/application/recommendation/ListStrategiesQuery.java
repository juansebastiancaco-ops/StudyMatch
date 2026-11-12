package co.ufps.studymatch.application.recommendation;

import co.ufps.studymatch.recommendation.CompositeRecommendationStrategy;
import co.ufps.studymatch.recommendation.RecommendationStrategy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ListStrategiesQuery {
    private final RecommendationStrategyRegistry registry;

    @Inject
    public ListStrategiesQuery(RecommendationStrategyRegistry registry) {
        this.registry = registry;
    }

    public List<StrategyInfoDTO> execute() {
        List<StrategyInfoDTO> base = registry.baseStrategies().stream()
                .map(strategy -> new StrategyInfoDTO(strategy.name(), strategy.description(), false, Map.of()))
                .toList();
        CompositeRecommendationStrategy composite = registry.composite();
        StrategyInfoDTO compositeInfo = new StrategyInfoDTO(
                composite.name(),
                composite.description(),
                true,
                composite.getWeights()
        );
        return java.util.stream.Stream.concat(base.stream(), java.util.stream.Stream.of(compositeInfo)).toList();
    }
}
