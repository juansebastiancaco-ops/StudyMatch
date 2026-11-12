package co.ufps.studymatch.application.common;

import co.ufps.studymatch.ValidationException;
import co.ufps.studymatch.student.AvailabilitySlot;

/**
 * DTO de entrada para horarios. La conversion a Value Object vive en el dominio.
 */
public record AvailabilitySlotInput(String day, String startTime, String endTime) {
    public AvailabilitySlotInput {
        if (day == null || day.isBlank()) {
            throw new ValidationException("El dia del horario es obligatorio");
        }
        if (startTime == null || startTime.isBlank() || endTime == null || endTime.isBlank()) {
            throw new ValidationException("La hora de inicio y fin son obligatorias");
        }
    }

    public AvailabilitySlot toDomain() {
        return AvailabilitySlot.create(day, startTime, endTime);
    }
}
