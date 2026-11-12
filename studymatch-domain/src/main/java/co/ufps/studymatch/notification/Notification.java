package co.ufps.studymatch.notification;

import co.ufps.studymatch.ValidationException;
import co.ufps.studymatch.student.StudentId;

import java.time.LocalDateTime;
import java.util.Objects;

public class Notification {
    private final NotificationId notificationId;
    private final StudentId studentId;
    private final NotificationType type;
    private final String message;
    private boolean read;
    private final LocalDateTime createdAt;
    private final String relatedId;

    private Notification(NotificationId notificationId, StudentId studentId, NotificationType type,
                         String message, boolean read, LocalDateTime createdAt, String relatedId) {
        this.notificationId = Objects.requireNonNull(notificationId, "El notificationId es obligatorio");
        this.studentId = Objects.requireNonNull(studentId, "El studentId es obligatorio");
        this.type = Objects.requireNonNull(type, "El tipo de notificacion es obligatorio");
        if (message == null || message.isBlank()) {
            throw new ValidationException("El mensaje de la notificacion es obligatorio");
        }
        this.message = message.strip();
        this.read = read;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.relatedId = relatedId;
    }

    public static Notification create(StudentId studentId, NotificationType type, String message, String relatedId) {
        return new Notification(NotificationId.generate(), studentId, type, message, false,
                LocalDateTime.now(), relatedId);
    }

    public static Notification of(NotificationId notificationId, StudentId studentId, NotificationType type,
                                  String message, boolean read, LocalDateTime createdAt, String relatedId) {
        return new Notification(notificationId, studentId, type, message, read, createdAt, relatedId);
    }

    public void markRead() {
        this.read = true;
    }

    public NotificationId getNotificationId() {
        return notificationId;
    }

    public StudentId getStudentId() {
        return studentId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getRelatedId() {
        return relatedId;
    }
}
