package co.ufps.studymatch.application.notification;

import co.ufps.studymatch.notification.Notification;

import java.time.LocalDateTime;

public record NotificationDTO(
        String notificationId,
        String studentId,
        String type,
        String message,
        boolean read,
        LocalDateTime createdAt,
        String relatedId
) {
    public static NotificationDTO from(Notification notification) {
        return new NotificationDTO(
                notification.getNotificationId().value(),
                notification.getStudentId().value(),
                notification.getType().name(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getRelatedId()
        );
    }
}
