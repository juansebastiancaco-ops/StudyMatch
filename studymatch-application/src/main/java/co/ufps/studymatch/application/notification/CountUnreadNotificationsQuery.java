package co.ufps.studymatch.application.notification;

import co.ufps.studymatch.notification.INotificationRepository;
import co.ufps.studymatch.student.StudentId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CountUnreadNotificationsQuery {
    private final INotificationRepository notificationRepository;

    @Inject
    public CountUnreadNotificationsQuery(INotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public long execute(String studentId) {
        return notificationRepository.contarNoLeidas(StudentId.of(studentId));
    }
}
