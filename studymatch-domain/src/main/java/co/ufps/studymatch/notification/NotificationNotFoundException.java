package co.ufps.studymatch.notification;

import co.ufps.studymatch.NotFoundException;

public class NotificationNotFoundException extends NotFoundException {
    public NotificationNotFoundException(String notificationId) {
        super("Notificacion no encontrada con id: " + notificationId);
    }
}
