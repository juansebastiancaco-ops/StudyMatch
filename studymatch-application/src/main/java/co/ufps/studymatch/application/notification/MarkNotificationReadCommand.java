package co.ufps.studymatch.application.notification;

import co.ufps.studymatch.ValidationException;

public record MarkNotificationReadCommand(String studentId, String notificationId) {
    public MarkNotificationReadCommand {
        if (studentId == null || studentId.isBlank()) {
            throw new ValidationException("El studentId es obligatorio");
        }
        if (notificationId == null || notificationId.isBlank()) {
            throw new ValidationException("El notificationId es obligatorio");
        }
    }
}
