package co.ufps.studymatch.infrastructure.persistence.notification;

import co.ufps.studymatch.notification.INotificationRepository;
import co.ufps.studymatch.notification.Notification;
import co.ufps.studymatch.notification.NotificationId;
import co.ufps.studymatch.notification.NotificationType;
import co.ufps.studymatch.student.StudentId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class NotificationRepositoryJpa implements INotificationRepository {
    @PersistenceContext
    EntityManager entityManager;

    @Override
    @Transactional
    public Notification guardar(Notification notification) {
        entityManager.merge(toEntity(notification));
        entityManager.flush();
        entityManager.clear();
        return notification;
    }

    @Override
    public Optional<Notification> buscarId(NotificationId notificationId) {
        NotificationJpa entity = entityManager.find(NotificationJpa.class, notificationId.value());
        return entity == null ? Optional.empty() : Optional.of(toDomain(entity));
    }

    @Override
    public List<Notification> buscarPorEstudiante(StudentId studentId, boolean onlyUnread) {
        String query = "SELECT n FROM NotificationJpa n WHERE n.studentId = :studentId";
        if (onlyUnread) {
            query += " AND n.read = false";
        }
        query += " ORDER BY n.createdAt DESC";
        return entityManager.createQuery(query, NotificationJpa.class)
                .setParameter("studentId", studentId.value())
                .getResultList().stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional
    public void marcarTodasLeidas(StudentId studentId) {
        entityManager.createQuery(
                        "UPDATE NotificationJpa n SET n.read = true WHERE n.studentId = :studentId AND n.read = false")
                .setParameter("studentId", studentId.value())
                .executeUpdate();
        entityManager.clear();
    }

    @Override
    public long contarNoLeidas(StudentId studentId) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(n) FROM NotificationJpa n WHERE n.studentId = :studentId AND n.read = false",
                        Long.class)
                .setParameter("studentId", studentId.value())
                .getSingleResult();
        return count;
    }

    private NotificationJpa toEntity(Notification notification) {
        NotificationJpa entity = new NotificationJpa();
        entity.setNotificationId(notification.getNotificationId().value());
        entity.setStudentId(notification.getStudentId().value());
        entity.setType(notification.getType().name());
        entity.setMessage(notification.getMessage());
        entity.setRead(notification.isRead());
        entity.setCreatedAt(notification.getCreatedAt());
        entity.setRelatedId(notification.getRelatedId());
        return entity;
    }

    private Notification toDomain(NotificationJpa entity) {
        return Notification.of(
                NotificationId.of(entity.getNotificationId()),
                StudentId.of(entity.getStudentId()),
                NotificationType.valueOf(entity.getType()),
                entity.getMessage(),
                entity.isRead(),
                entity.getCreatedAt(),
                entity.getRelatedId()
        );
    }
}
