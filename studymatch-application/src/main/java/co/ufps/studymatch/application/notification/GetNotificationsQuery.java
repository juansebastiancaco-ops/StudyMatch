package co.ufps.studymatch.application.notification;

import co.ufps.studymatch.notification.INotificationRepository;
import co.ufps.studymatch.student.StudentId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class GetNotificationsQuery {
    private final INotificationRepository notificationRepository;

    @Inject
    public GetNotificationsQuery(INotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<NotificationDTO> execute(String studentId, boolean onlyUnread) {
        return notificationRepository.buscarPorEstudiante(StudentId.of(studentId), onlyUnread).stream()
                .map(NotificationDTO::from)
                .toList();
    }
}
