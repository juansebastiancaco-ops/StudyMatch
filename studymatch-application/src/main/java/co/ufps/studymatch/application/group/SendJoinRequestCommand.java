package co.ufps.studymatch.application.group;

import co.ufps.studymatch.ValidationException;

public record SendJoinRequestCommand(String studentId, String groupId, String message) {
    public SendJoinRequestCommand {
        if (studentId == null || studentId.isBlank()) {
            throw new ValidationException("El studentId es obligatorio");
        }
        if (groupId == null || groupId.isBlank()) {
            throw new ValidationException("El groupId es obligatorio");
        }
    }
}
