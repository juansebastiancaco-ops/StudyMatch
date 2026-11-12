package co.ufps.studymatch.infrastructure.config;

import co.ufps.studymatch.recommendation.RecommendationWeights;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class RecommendationWeightsProducer {
    @Produces
    @Singleton
    public RecommendationWeights weights(
            @ConfigProperty(name = "studymatch.recommendation.weights.subject", defaultValue = "0.35") double subject,
            @ConfigProperty(name = "studymatch.recommendation.weights.schedule", defaultValue = "0.30") double schedule,
            @ConfigProperty(name = "studymatch.recommendation.weights.career", defaultValue = "0.20") double career,
            @ConfigProperty(name = "studymatch.recommendation.weights.academic", defaultValue = "0.15") double academic) {
        return new RecommendationWeights(subject, schedule, career, academic);
    }
}
