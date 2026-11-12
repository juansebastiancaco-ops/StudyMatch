package co.ufps.studymatch.application.notification;

import co.ufps.studymatch.application.common.ForbiddenOperationException;
import co.ufps.studymatch.notification.INotificationRepository;
import co.ufps.studymatch.notification.Notification;
import co.ufps.studymatch.notification.NotificationId;
import co.ufps.studymatch.notification.NotificationNotFoundException;
import co.ufps.studymatch.student.StudentId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MarkNotificationReadHandler {
    private final INotificationRepository notificationRepository;

    @Inject
    public MarkNotificationReadHandler(INotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public NotificationDTO execute(MarkNotificationReadCommand command) {
        StudentId studentId = StudentId.of(command.studentId());
        Notification notification = notificationRepository.buscarId(NotificationId.of(command.notificationId()))
                .orElseThrow(() -> new NotificationNotFoundException(command.notificationId()));
        if (!notification.getStudentId().equals(studentId)) {
            throw new ForbiddenOperationException("No puedes modificar notificaciones de otro estudiante");
        }
        notification.markRead();
        return NotificationDTO.from(notificationRepository.guardar(notification));
    }
}
