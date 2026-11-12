package co.ufps.studymatch.application.group;

import co.ufps.studymatch.ValidationException;

public record LeaveGroupCommand(String studentId, String groupId) {
    public LeaveGroupCommand {
        if (studentId == null || studentId.isBlank()) {
            throw new ValidationException("El studentId es obligatorio");
        }
        if (groupId == null || groupId.isBlank()) {
            throw new ValidationException("El groupId es obligatorio");
        }
    }
}
