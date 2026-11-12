package co.ufps.studymatch.recommendation;

import co.ufps.studymatch.ValidationException;

import java.util.Locale;

/**
 * Value Object que representa el porcentaje de afinidad (0 a 100).
 */
public record RecommendationScore(double value) {
    public RecommendationScore {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new ValidationException("El puntaje de recomendacion debe ser un numero");
        }
        if (value < 0 || value > 100) {
            throw new ValidationException("El puntaje de recomendacion debe estar entre 0 y 100");
        }
    }

    public static RecommendationScore of(double value) {
        double clamped = Math.max(0, Math.min(100, value));
        return new RecommendationScore(Math.round(clamped * 100.0) / 100.0);
    }

    public static RecommendationScore zero() {
        return new RecommendationScore(0);
    }

    public String formatted() {
        return String.format(Locale.US, "%.0f%%", value);
    }
}
