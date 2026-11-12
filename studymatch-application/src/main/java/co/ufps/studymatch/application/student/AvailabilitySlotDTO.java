package co.ufps.studymatch.application.student;

import co.ufps.studymatch.student.AvailabilitySlot;
import co.ufps.studymatch.student.TimeRange;

public record AvailabilitySlotDTO(
        String slotId,
        String day,
        String startTime,
        String endTime
) {
    public static AvailabilitySlotDTO from(AvailabilitySlot slot) {
        TimeRange range = slot.timeRange();
        return new AvailabilitySlotDTO(
                slot.slotId(),
                slot.dayOfWeek().name(),
                range.start().toString(),
                range.end().toString()
        );
    }
}
