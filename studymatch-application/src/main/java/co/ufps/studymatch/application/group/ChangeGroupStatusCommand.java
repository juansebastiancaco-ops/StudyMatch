package co.ufps.studymatch.application.group;

import co.ufps.studymatch.ValidationException;

public record ChangeGroupStatusCommand(String actorId, String groupId, String status) {
    public ChangeGroupStatusCommand {
        if (actorId == null || actorId.isBlank()) {
            throw new ValidationException("El actorId es obligatorio");
        }
        if (groupId == null || groupId.isBlank()) {
            throw new ValidationException("El groupId es obligatorio");
        }
        if (status == null || status.isBlank()) {
            throw new ValidationException("El estado es obligatorio");
        }
    }
}
