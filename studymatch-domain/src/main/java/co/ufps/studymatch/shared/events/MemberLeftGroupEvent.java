package co.ufps.studymatch.shared.events;

import co.ufps.studymatch.notification.NotificationType;
import co.ufps.studymatch.shared.DomainEvent;
import co.ufps.studymatch.student.StudentId;

import java.time.Instant;

public record MemberLeftGroupEvent(
        StudentId recipientId,
        String message,
        String relatedId,
        Instant occurredAt
) implements DomainEvent {
    public MemberLeftGroupEvent(StudentId leaderId, String message, String groupId) {
        this(leaderId, message, groupId, Instant.now());
    }

    @Override
    public NotificationType notificationType() {
        return NotificationType.MEMBER_LEFT_GROUP;
    }
}
