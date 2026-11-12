package co.ufps.studymatch.student;

import co.ufps.studymatch.ValidationException;

import java.time.DayOfWeek;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public final class AvailabilitySlot {
    private final String slotId;
    private final DayOfWeek dayOfWeek;
    private final TimeRange timeRange;

    private AvailabilitySlot(String slotId, DayOfWeek dayOfWeek, TimeRange timeRange) {
        if (slotId == null || slotId.isBlank()) {
            throw new ValidationException("El slotId no puede estar vacio");
        }
        this.slotId = slotId.strip();
        this.dayOfWeek = Objects.requireNonNull(dayOfWeek, "El dia es obligatorio");
        this.timeRange = Objects.requireNonNull(timeRange, "El rango horario es obligatorio");
    }

    public static AvailabilitySlot create(DayOfWeek dayOfWeek, TimeRange timeRange) {
        return new AvailabilitySlot(UUID.randomUUID().toString(), dayOfWeek, timeRange);
    }

    public static AvailabilitySlot of(String slotId, DayOfWeek dayOfWeek, TimeRange timeRange) {
        return new AvailabilitySlot(slotId, dayOfWeek, timeRange);
    }

    public static AvailabilitySlot create(String day, String start, String end) {
        return create(parseDay(day), TimeRange.of(start, end));
    }

    public static DayOfWeek parseDay(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("El dia de la semana es obligatorio");
        }
        String normalized = value.strip().toUpperCase(Locale.ROOT)
                .replace(".", "")
                .replace(" ", "_");
        return switch (normalized) {
            case "MONDAY", "LUNES", "LUN" -> DayOfWeek.MONDAY;
            case "TUESDAY", "MARTES", "MAR" -> DayOfWeek.TUESDAY;
            case "WEDNESDAY", "MIERCOLES", "MIE" -> DayOfWeek.WEDNESDAY;
            case "THURSDAY", "JUEVES", "JUE" -> DayOfWeek.THURSDAY;
            case "FRIDAY", "VIERNES", "VIE" -> DayOfWeek.FRIDAY;
            case "SATURDAY", "SABADO", "SAB" -> DayOfWeek.SATURDAY;
            case "SUNDAY", "DOMINGO", "DOM" -> DayOfWeek.SUNDAY;
            default -> throw new ValidationException("Dia de la semana invalido: " + value);
        };
    }

    public boolean overlaps(AvailabilitySlot other) {
        return dayOfWeek == other.dayOfWeek && timeRange.overlaps(other.timeRange);
    }

    public long overlapMinutes(AvailabilitySlot other) {
        if (dayOfWeek != other.dayOfWeek) {
            return 0;
        }
        return timeRange.overlapMinutes(other.timeRange);
    }

    public String slotId() {
        return slotId;
    }

    public DayOfWeek dayOfWeek() {
        return dayOfWeek;
    }

    public TimeRange timeRange() {
        return timeRange;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AvailabilitySlot that)) return false;
        return slotId.equals(that.slotId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slotId);
    }
}
