package co.ufps.studymatch.application.group;

import co.ufps.studymatch.ValidationException;

public record RejectJoinRequestCommand(String leaderId, String groupId, String requestId) {
    public RejectJoinRequestCommand {
        if (leaderId == null || leaderId.isBlank()) {
            throw new ValidationException("El leaderId es obligatorio");
        }
        if (groupId == null || groupId.isBlank()) {
            throw new ValidationException("El groupId es obligatorio");
        }
        if (requestId == null || requestId.isBlank()) {
            throw new ValidationException("El requestId es obligatorio");
        }
    }
}
