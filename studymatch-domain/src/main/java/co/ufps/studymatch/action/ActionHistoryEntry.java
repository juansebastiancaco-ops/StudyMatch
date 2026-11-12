package co.ufps.studymatch.action;

import co.ufps.studymatch.ValidationException;
import co.ufps.studymatch.student.StudentId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Registro de una accion reversible. El comando vivo vive en memoria; este registro permite
 * auditar el historial aunque la aplicacion se reinicie.
 */
public class ActionHistoryEntry {
    private final String actionId;
    private final StudentId studentId;
    private final String type;
    private final String description;
    private final LocalDateTime createdAt;
    private boolean undone;

    private ActionHistoryEntry(String actionId, StudentId studentId, String type, String description,
                               LocalDateTime createdAt, boolean undone) {
        if (actionId == null || actionId.isBlank()) {
            throw new ValidationException("El actionId es obligatorio");
        }
        this.actionId = actionId.strip();
        this.studentId = Objects.requireNonNull(studentId, "El studentId es obligatorio");
        if (type == null || type.isBlank()) {
            throw new ValidationException("El tipo de accion es obligatorio");
        }
        this.type = type.strip();
        if (description == null || description.isBlank()) {
            throw new ValidationException("La descripcion de la accion es obligatoria");
        }
        this.description = description.strip();
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.undone = undone;
    }

    public static ActionHistoryEntry create(String actionId, StudentId studentId, String type, String description) {
        return new ActionHistoryEntry(actionId, studentId, type, description, LocalDateTime.now(), false);
    }

    public static ActionHistoryEntry of(String actionId, StudentId studentId, String type, String description,
                                        LocalDateTime createdAt, boolean undone) {
        return new ActionHistoryEntry(actionId, studentId, type, description, createdAt, undone);
    }

    public void markUndone() {
        this.undone = true;
    }

    public String getActionId() {
        return actionId;
    }

    public StudentId getStudentId() {
        return studentId;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isUndone() {
        return undone;
    }
}
