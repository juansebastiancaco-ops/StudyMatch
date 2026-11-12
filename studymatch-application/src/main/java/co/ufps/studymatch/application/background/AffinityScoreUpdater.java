package co.ufps.studymatch.application.background;

import co.ufps.studymatch.application.recommendation.RecommendationCache;
import co.ufps.studymatch.application.recommendation.RecommendationService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Worker que recalcula los puntajes de afinidad en segundo plano con virtual threads.
 */
@ApplicationScoped
public class AffinityScoreUpdater {
    private static final Logger LOGGER = Logger.getLogger(AffinityScoreUpdater.class.getName());

    private final RecommendationService recommendationService;
    private final RecommendationCache cache;
    private final long intervalSeconds;
    private final ExecutorService executor;
    private volatile boolean running = true;

    @Inject
    public AffinityScoreUpdater(RecommendationService recommendationService,
                                RecommendationCache cache,
                                @ConfigProperty(name = "studymatch.workers.affinity.seconds", defaultValue = "60")
                                long intervalSeconds) {
        this.recommendationService = recommendationService;
        this.cache = cache;
        this.intervalSeconds = intervalSeconds;
        this.executor = Executors.newSingleThreadExecutor(
                Thread.ofVirtual().name("studymatch-affinity-updater-", 0).factory());
    }

    @PostConstruct
    void start() {
        executor.submit(this::loop);
    }

    @PreDestroy
    void stop() {
        running = false;
        executor.shutdownNow();
    }

    public int runOnce() {
        return recommendationService.refreshAffinityScores();
    }

    private void loop() {
        long interval = Math.max(1, intervalSeconds);
        while (running) {
            try {
                runOnce();
            } catch (Throwable e) {
                LOGGER.log(Level.WARNING, "Fallo el recalculado de afinidad; se reintentara", e);
            }
            if (!sleep(interval)) {
                return;
            }
        }
    }

    private boolean sleep(long seconds) {
        try {
            Thread.sleep(Math.max(1, seconds) * 1000L);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    RecommendationCache cache() {
        return cache;
    }
}
