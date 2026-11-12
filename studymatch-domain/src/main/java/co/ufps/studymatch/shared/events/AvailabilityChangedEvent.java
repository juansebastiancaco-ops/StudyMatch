package co.ufps.studymatch.shared.events;

import co.ufps.studymatch.notification.NotificationType;
import co.ufps.studymatch.shared.DomainEvent;
import co.ufps.studymatch.student.StudentId;

import java.time.Instant;

public record AvailabilityChangedEvent(
        StudentId recipientId,
        String message,
        String relatedId,
        Instant occurredAt
) implements DomainEvent {
    public AvailabilityChangedEvent(StudentId studentId, String message) {
        this(studentId, message, studentId.value(), Instant.now());
    }

    @Override
    public NotificationType notificationType() {
        return NotificationType.AVAILABILITY_CHANGED;
    }
}
