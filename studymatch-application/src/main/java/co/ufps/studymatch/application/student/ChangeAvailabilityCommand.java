package co.ufps.studymatch.application.student;

import co.ufps.studymatch.ValidationException;
import co.ufps.studymatch.application.common.AvailabilitySlotInput;

import java.util.List;

public record ChangeAvailabilityCommand(
        String studentId,
        String availabilityStatus,
        List<AvailabilitySlotInput> availabilitySlots
) {
    public ChangeAvailabilityCommand {
        if (studentId == null || studentId.isBlank()) {
            throw new ValidationException("El studentId es obligatorio");
        }
        if (availabilityStatus == null || availabilityStatus.isBlank()) {
            throw new ValidationException("La disponibilidad es obligatoria");
        }
        availabilitySlots = availabilitySlots == null ? List.of() : List.copyOf(availabilitySlots);
    }
}
