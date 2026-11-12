package co.ufps.studymatch.shared.events;

import co.ufps.studymatch.notification.NotificationType;
import co.ufps.studymatch.shared.DomainEvent;
import co.ufps.studymatch.student.StudentId;

import java.time.Instant;

public record JoinRequestRejectedEvent(
        StudentId recipientId,
        String message,
        String relatedId,
        Instant occurredAt
) implements DomainEvent {
    public JoinRequestRejectedEvent(StudentId studentId, String message, String requestId) {
        this(studentId, message, requestId, Instant.now());
    }

    @Override
    public NotificationType notificationType() {
        return NotificationType.JOIN_REQUEST_REJECTED;
    }
}
