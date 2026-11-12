package co.ufps.studymatch.infrastructure.persistence.action;

import co.ufps.studymatch.action.ActionHistoryEntry;
import co.ufps.studymatch.action.IActionHistoryRepository;
import co.ufps.studymatch.student.StudentId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ActionHistoryRepositoryJpa implements IActionHistoryRepository {
    @PersistenceContext
    EntityManager entityManager;

    @Override
    @Transactional
    public ActionHistoryEntry guardar(ActionHistoryEntry entry) {
        entityManager.merge(toEntity(entry));
        entityManager.flush();
        entityManager.clear();
        return entry;
    }

    @Override
    public Optional<ActionHistoryEntry> buscarId(String actionId) {
        ActionHistoryJpa entity = entityManager.find(ActionHistoryJpa.class, actionId);
        return entity == null ? Optional.empty() : Optional.of(toDomain(entity));
    }

    @Override
    public List<ActionHistoryEntry> buscarPorEstudiante(StudentId studentId) {
        return entityManager.createQuery(
                        "SELECT a FROM ActionHistoryJpa a WHERE a.studentId = :studentId ORDER BY a.createdAt DESC",
                        ActionHistoryJpa.class)
                .setParameter("studentId", studentId.value())
                .getResultList().stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional
    public void marcarDeshecha(String actionId) {
        entityManager.createQuery(
                        "UPDATE ActionHistoryJpa a SET a.undone = true WHERE a.actionId = :actionId")
                .setParameter("actionId", actionId)
                .executeUpdate();
        entityManager.clear();
    }

    private ActionHistoryJpa toEntity(ActionHistoryEntry entry) {
        ActionHistoryJpa entity = new ActionHistoryJpa();
        entity.setActionId(entry.getActionId());
        entity.setStudentId(entry.getStudentId().value());
        entity.setType(entry.getType());
        entity.setDescription(entry.getDescription());
        entity.setCreatedAt(entry.getCreatedAt());
        entity.setUndone(entry.isUndone());
        return entity;
    }

    private ActionHistoryEntry toDomain(ActionHistoryJpa entity) {
        return ActionHistoryEntry.of(
                entity.getActionId(),
                StudentId.of(entity.getStudentId()),
                entity.getType(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.isUndone()
        );
    }
}
