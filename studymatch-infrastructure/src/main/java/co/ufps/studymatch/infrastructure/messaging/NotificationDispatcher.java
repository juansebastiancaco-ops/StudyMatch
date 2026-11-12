package co.ufps.studymatch.infrastructure.messaging;

import co.ufps.studymatch.notification.NotificationObserver;
import co.ufps.studymatch.shared.DomainEvent;
import co.ufps.studymatch.shared.DomainEventPublisher;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sujeto del patron Observer. Publica de forma asincrona con virtual threads para no bloquear
 * el hilo HTTP. Los eventos se publican despues de persistir el cambio de estado.
 */
@ApplicationScoped
public class NotificationDispatcher implements DomainEventPublisher {
    private static final Logger LOGGER = Logger.getLogger(NotificationDispatcher.class.getName());

    private final CopyOnWriteArrayList<NotificationObserver> observers;
    private final ExecutorService executor;

    @Inject
    public NotificationDispatcher(Instance<NotificationObserver> observerInstances) {
        this(toList(observerInstances));
    }

    public NotificationDispatcher(Collection<NotificationObserver> observers) {
        this.observers = new CopyOnWriteArrayList<>(observers);
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    private static List<NotificationObserver> toList(Instance<NotificationObserver> instances) {
        return instances.stream().toList();
    }

    @Override
    public void publish(DomainEvent event) {
        if (event == null) {
            return;
        }
        for (NotificationObserver observer : observers) {
            try {
                executor.submit(() -> dispatch(observer, event));
            } catch (RejectedExecutionException e) {
                LOGGER.log(Level.FINE, "El dispatcher esta apagado; evento descartado", e);
            }
        }
    }

    private void dispatch(NotificationObserver observer, DomainEvent event) {
        try {
            observer.onEvent(event);
        } catch (Throwable e) {
            LOGGER.log(Level.WARNING, "El observador " + observer.observerName()
                    + " fallo al procesar " + event.notificationType(), e);
        }
    }

    @Override
    public void subscribe(NotificationObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void unsubscribe(NotificationObserver observer) {
        observers.remove(observer);
    }

    public int observerCount() {
        return observers.size();
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }
}
