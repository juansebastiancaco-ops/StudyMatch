package co.ufps.studymatch.notification;

import co.ufps.studymatch.student.StudentId;

import java.util.List;
import java.util.Optional;

public interface INotificationRepository {
    Notification guardar(Notification notification);

    Optional<Notification> buscarId(NotificationId notificationId);

    List<Notification> buscarPorEstudiante(StudentId studentId, boolean onlyUnread);

    void marcarTodasLeidas(StudentId studentId);

    long contarNoLeidas(StudentId studentId);
}
