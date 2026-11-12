package co.ufps.studymatch.shared.events;

import co.ufps.studymatch.notification.NotificationType;
import co.ufps.studymatch.shared.DomainEvent;
import co.ufps.studymatch.student.StudentId;

import java.time.Instant;

public record JoinRequestSentEvent(
        StudentId recipientId,
        String message,
        String relatedId,
        Instant occurredAt
) implements DomainEvent {
    public JoinRequestSentEvent(StudentId leaderId, String message, String requestId) {
        this(leaderId, message, requestId, Instant.now());
    }

    @Override
    public NotificationType notificationType() {
        return NotificationType.JOIN_REQUEST_SENT;
    }
}
