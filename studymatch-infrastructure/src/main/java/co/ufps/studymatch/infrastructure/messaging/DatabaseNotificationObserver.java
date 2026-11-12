package co.ufps.studymatch.infrastructure.messaging;

import co.ufps.studymatch.notification.INotificationRepository;
import co.ufps.studymatch.notification.Notification;
import co.ufps.studymatch.notification.NotificationObserver;
import co.ufps.studymatch.shared.DomainEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class DatabaseNotificationObserver implements NotificationObserver {
    private final INotificationRepository notificationRepository;

    @Inject
    public DatabaseNotificationObserver(INotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void onEvent(DomainEvent event) {
        if (event.recipientId() == null) {
            return;
        }
        notificationRepository.guardar(Notification.create(
                event.recipientId(),
                event.notificationType(),
                event.message(),
                event.relatedId()
        ));
    }

    @Override
    public String observerName() {
        return "database-notification-observer";
    }
}
