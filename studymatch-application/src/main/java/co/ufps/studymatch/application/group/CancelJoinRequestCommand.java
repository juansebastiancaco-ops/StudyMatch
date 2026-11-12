package co.ufps.studymatch.application.group;

import co.ufps.studymatch.ValidationException;

public record CancelJoinRequestCommand(String studentId, String groupId, String requestId) {
    public CancelJoinRequestCommand {
        if (studentId == null || studentId.isBlank()) {
            throw new ValidationException("El studentId es obligatorio");
        }
        if (groupId == null || groupId.isBlank()) {
            throw new ValidationException("El groupId es obligatorio");
        }
        if (requestId == null || requestId.isBlank()) {
            throw new ValidationException("El requestId es obligatorio");
        }
    }
}
