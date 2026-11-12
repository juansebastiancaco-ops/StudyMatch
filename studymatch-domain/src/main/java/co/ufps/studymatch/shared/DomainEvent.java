package co.ufps.studymatch.shared;

import co.ufps.studymatch.notification.NotificationType;
import co.ufps.studymatch.student.StudentId;

import java.time.Instant;

/**
 * Evento de dominio. El dominio define que ocurrio; infraestructura decide como despacharlo.
 * El destinatario permite que el Observer sepa a que estudiante notificar.
 */
public interface DomainEvent {
    StudentId recipientId();
    NotificationType notificationType();
    String message();
    String relatedId();
    Instant occurredAt();
}
