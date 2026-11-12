package co.ufps.studymatch.shared.events;

import co.ufps.studymatch.notification.NotificationType;
import co.ufps.studymatch.shared.DomainEvent;
import co.ufps.studymatch.student.StudentId;

import java.time.Instant;

public record StudentRegisteredEvent(
        StudentId recipientId,
        String message,
        String relatedId,
        Instant occurredAt
) implements DomainEvent {
    public StudentRegisteredEvent(StudentId recipientId, String message) {
        this(recipientId, message, recipientId.value(), Instant.now());
    }

    @Override
    public NotificationType notificationType() {
        return NotificationType.WELCOME;
    }
}
