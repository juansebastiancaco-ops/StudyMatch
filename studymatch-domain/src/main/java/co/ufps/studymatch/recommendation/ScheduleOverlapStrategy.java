package co.ufps.studymatch.recommendation;

import co.ufps.studymatch.student.AvailabilitySlot;

import java.util.List;

public final class ScheduleOverlapStrategy implements RecommendationStrategy {
    public static final String NAME = "schedule";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String description() {
        return "Coincidencia entre la disponibilidad del estudiante y los horarios del candidato o grupo";
    }

    @Override
    public RecommendationScore score(RecommendationContext context) {
        List<AvailabilitySlot> sourceSlots = context.source().getAvailabilitySlots();
        if (sourceSlots.isEmpty()) {
            return RecommendationScore.zero();
        }
        List<AvailabilitySlot> targetSlots = context.group() != null
                ? context.group().getSchedule()
                : context.candidate() != null ? context.candidate().getAvailabilitySlots() : List.of();
        if (targetSlots.isEmpty()) {
            return RecommendationScore.zero();
        }
        long overlap = totalOverlapMinutes(sourceSlots, targetSlots);
        long sourceTotal = totalMinutes(sourceSlots);
        if (sourceTotal <= 0) {
            return RecommendationScore.zero();
        }
        return RecommendationScore.of((overlap * 100.0) / sourceTotal);
    }

    private long totalOverlapMinutes(List<AvailabilitySlot> first, List<AvailabilitySlot> second) {
        long total = 0;
        for (AvailabilitySlot slot : first) {
            for (AvailabilitySlot candidate : second) {
                total += slot.overlapMinutes(candidate);
            }
        }
        return total;
    }

    private long totalMinutes(List<AvailabilitySlot> slots) {
        return slots.stream().mapToLong(slot -> slot.timeRange().durationMinutes()).sum();
    }
}
