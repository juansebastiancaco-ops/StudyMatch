package co.ufps.studymatch.shared.events;

import co.ufps.studymatch.notification.NotificationType;
import co.ufps.studymatch.shared.DomainEvent;
import co.ufps.studymatch.student.StudentId;

import java.time.Instant;

public record GroupCreatedEvent(
        StudentId recipientId,
        String message,
        String relatedId,
        Instant occurredAt
) implements DomainEvent {
    public GroupCreatedEvent(StudentId recipientId, String message, String groupId) {
        this(recipientId, message, groupId, Instant.now());
    }

    @Override
    public NotificationType notificationType() {
        return NotificationType.GROUP_CREATED;
    }
}
