package co.ufps.studymatch.student;

import co.ufps.studymatch.ValidationException;

import java.time.LocalTime;
import java.util.Objects;

public record TimeRange(LocalTime start, LocalTime end) {
    public TimeRange {
        if (start == null || end == null) {
            throw new ValidationException("La hora de inicio y fin son obligatorias");
        }
        if (!start.isBefore(end)) {
            throw new ValidationException("La hora de inicio debe ser anterior a la hora de fin");
        }
    }

    public static TimeRange of(String start, String end) {
        return new TimeRange(parseTime(start), parseTime(end));
    }

    public static LocalTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("La hora es obligatoria");
        }
        try {
            String normalized = value.strip();
            if (normalized.length() == 4 && normalized.charAt(1) == ':') {
                normalized = "0" + normalized;
            }
            return LocalTime.parse(normalized.length() == 5 ? normalized : normalized.substring(0, 8));
        } catch (RuntimeException e) {
            throw new ValidationException("Formato de hora invalido: " + value + ". Use HH:mm o HH:mm:ss");
        }
    }

    public boolean overlaps(TimeRange other) {
        return start.isBefore(other.end) && other.start.isBefore(end);
    }

    public long overlapMinutes(TimeRange other) {
        if (!overlaps(other)) {
            return 0;
        }
        LocalTime from = start.isAfter(other.start) ? start : other.start;
        LocalTime to = end.isBefore(other.end) ? end : other.end;
        return java.time.Duration.between(from, to).toMinutes();
    }

    public long durationMinutes() {
        return java.time.Duration.between(start, end).toMinutes();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeRange timeRange)) return false;
        return start.equals(timeRange.start) && end.equals(timeRange.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}
