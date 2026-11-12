package co.ufps.studymatch.application.notification;

import co.ufps.studymatch.ValidationException;

public record MarkAllNotificationsReadCommand(String studentId) {
    public MarkAllNotificationsReadCommand {
        if (studentId == null || studentId.isBlank()) {
            throw new ValidationException("El studentId es obligatorio");
        }
    }
}
