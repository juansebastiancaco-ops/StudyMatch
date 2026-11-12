package co.ufps.studymatch.infrastructure.messaging;

import co.ufps.studymatch.notification.NotificationObserver;
import co.ufps.studymatch.shared.DomainEvent;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.logging.Logger;

/**
 * Segundo observador: simula el envio de un correo/notificacion externa por consola.
 */
@ApplicationScoped
public class ConsoleNotificationObserver implements NotificationObserver {
    private static final Logger LOGGER = Logger.getLogger(ConsoleNotificationObserver.class.getName());

    @Override
    public void onEvent(DomainEvent event) {
        LOGGER.info(() -> "[StudyMatch][notificacion] para=" + event.recipientId().value()
                + " tipo=" + event.notificationType()
                + " mensaje=" + event.message());
    }

    @Override
    public String observerName() {
        return "console-notification-observer";
    }
}
