package co.ufps.studymatch.application.group;

import co.ufps.studymatch.ValidationException;
import co.ufps.studymatch.application.common.AvailabilitySlotInput;

import java.util.List;

public record CreateGroupCommand(
        String creatorId,
        String subjectId,
        String name,
        String description,
        int maxMembers,
        String modality,
        List<AvailabilitySlotInput> schedule
) {
    public CreateGroupCommand {
        if (creatorId == null || creatorId.isBlank()) {
            throw new ValidationException("El creatorId es obligatorio");
        }
        if (subjectId == null || subjectId.isBlank()) {
            throw new ValidationException("La asignatura es obligatoria");
        }
        if (name == null || name.isBlank()) {
            throw new ValidationException("El nombre del grupo es obligatorio");
        }
        if (maxMembers < 2 || maxMembers > 50) {
            throw new ValidationException("La capacidad del grupo debe estar entre 2 y 50");
        }
        if (schedule == null || schedule.isEmpty()) {
            throw new ValidationException("El grupo debe tener al menos un horario");
        }
        schedule = List.copyOf(schedule);
    }
}
