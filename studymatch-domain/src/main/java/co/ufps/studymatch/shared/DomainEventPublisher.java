package co.ufps.studymatch.shared;

import co.ufps.studymatch.notification.NotificationObserver;

/**
 * Puerto del patron Observer (sujeto). Las implementaciones se encargan del despacho asincrono.
 */
public interface DomainEventPublisher {
    void publish(DomainEvent event);
    void subscribe(NotificationObserver observer);
    void unsubscribe(NotificationObserver observer);
}
