package co.ufps.studymatch.group;

import co.ufps.studymatch.ValidationException;
import co.ufps.studymatch.student.StudentId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad interna del agregado StudyGroup. Guarda el ciclo de vida de una solicitud.
 */
public class JoinRequest {
    private final JoinRequestId requestId;
    private final StudyGroupId groupId;
    private final StudentId studentId;
    private final String message;
    private RequestStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    private JoinRequest(JoinRequestId requestId, StudyGroupId groupId, StudentId studentId,
                        String message, RequestStatus status, LocalDateTime createdAt,
                        LocalDateTime resolvedAt) {
        this.requestId = Objects.requireNonNull(requestId, "El requestId es obligatorio");
        this.groupId = Objects.requireNonNull(groupId, "El groupId es obligatorio");
        this.studentId = Objects.requireNonNull(studentId, "El studentId es obligatorio");
        String normalizedMessage = message == null ? "" : message.strip();
        if (normalizedMessage.length() > 500) {
            throw new ValidationException("El mensaje de la solicitud no puede superar 500 caracteres");
        }
        this.message = normalizedMessage;
        this.status = Objects.requireNonNull(status, "El estado de la solicitud es obligatorio");
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.resolvedAt = resolvedAt;
    }

    public static JoinRequest create(StudyGroupId groupId, StudentId studentId, String message) {
        return new JoinRequest(JoinRequestId.generate(), groupId, studentId, message,
                RequestStatus.PENDING, LocalDateTime.now(), null);
    }

    public static JoinRequest of(JoinRequestId requestId, StudyGroupId groupId, StudentId studentId,
                                 String message, RequestStatus status, LocalDateTime createdAt,
                                 LocalDateTime resolvedAt) {
        return new JoinRequest(requestId, groupId, studentId, message, status, createdAt, resolvedAt);
    }

    public void accept() {
        this.status = status.transitionTo(RequestStatus.ACCEPTED);
        this.resolvedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = status.transitionTo(RequestStatus.REJECTED);
        this.resolvedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = status.transitionTo(RequestStatus.CANCELLED);
        this.resolvedAt = LocalDateTime.now();
    }

    /**
     * Lo usa Command.undo(). Vuelve a dejar la solicitud pendiente sin validar la transicion.
     */
    public void reopen() {
        this.status = RequestStatus.PENDING;
        this.resolvedAt = null;
    }

    public boolean isPending() {
        return status == RequestStatus.PENDING;
    }

    public JoinRequestId getRequestId() {
        return requestId;
    }

    public StudyGroupId getGroupId() {
        return groupId;
    }

    public StudentId getStudentId() {
        return studentId;
    }

    public String getMessage() {
        return message;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
}
