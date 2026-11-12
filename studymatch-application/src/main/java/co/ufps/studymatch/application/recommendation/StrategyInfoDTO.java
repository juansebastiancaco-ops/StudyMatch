package co.ufps.studymatch.application.recommendation;

import java.util.Map;

public record StrategyInfoDTO(
        String name,
        String description,
        boolean composite,
        Map<String, Double> weights
) {
}
