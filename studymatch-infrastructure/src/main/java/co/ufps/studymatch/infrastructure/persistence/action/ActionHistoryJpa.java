package co.ufps.studymatch.infrastructure.persistence.action;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "action_history", indexes = {
        @Index(name = "idx_action_history_student", columnList = "student_id"),
        @Index(name = "idx_action_history_created", columnList = "created_at")
})
public class ActionHistoryJpa {
    @Id
    @Column(name = "action_id", length = 36, nullable = false)
    private String actionId;

    @Column(name = "student_id", length = 36, nullable = false)
    private String studentId;

    @Column(name = "type", length = 50, nullable = false)
    private String type;

    @Column(name = "description", length = 500, nullable = false)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "undone", nullable = false)
    private boolean undone;

    public ActionHistoryJpa() {
    }

    public String getActionId() { return actionId; }
    public void setActionId(String actionId) { this.actionId = actionId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public boolean isUndone() { return undone; }
    public void setUndone(boolean undone) { this.undone = undone; }
}
