package co.ufps.studymatch.application.notification;

import co.ufps.studymatch.notification.INotificationRepository;
import co.ufps.studymatch.student.StudentId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MarkAllNotificationsReadHandler {
    private final INotificationRepository notificationRepository;

    @Inject
    public MarkAllNotificationsReadHandler(INotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public long execute(MarkAllNotificationsReadCommand command) {
        StudentId studentId = StudentId.of(command.studentId());
        notificationRepository.marcarTodasLeidas(studentId);
        return notificationRepository.contarNoLeidas(studentId);
    }
}
