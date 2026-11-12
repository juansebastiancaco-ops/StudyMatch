package co.ufps.studymatch.notification;

import co.ufps.studymatch.shared.DomainEvent;

/**
 * Patron Observer: los observadores reaccionan a eventos de dominio ya persistidos.
 */
public interface NotificationObserver {
    void onEvent(DomainEvent event);

    default String observerName() {
        return getClass().getSimpleName();
    }
}
