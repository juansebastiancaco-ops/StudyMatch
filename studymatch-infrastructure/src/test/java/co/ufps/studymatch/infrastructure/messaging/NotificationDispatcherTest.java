package co.ufps.studymatch.infrastructure.messaging;

import co.ufps.studymatch.notification.NotificationObserver;
import co.ufps.studymatch.shared.DomainEvent;
import co.ufps.studymatch.shared.events.StudentRegisteredEvent;
import co.ufps.studymatch.student.StudentId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationDispatcherTest {

    @Test
    void despachaEventosDeFormaAsincronaATodosLosObservadores() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger notifications = new AtomicInteger();
        NotificationObserver first = observer("observer-1", latch, notifications);
        NotificationObserver second = observer("observer-2", latch, notifications);
        NotificationDispatcher dispatcher = new NotificationDispatcher(List.of(first, second));
        DomainEvent event = new StudentRegisteredEvent(StudentId.generate(), "Bienvenido a StudyMatch");

        dispatcher.publish(event);

        assertTrue(latch.await(3, TimeUnit.SECONDS), "Los observadores deben ejecutarse en segundo plano");
        assertEquals(2, notifications.get());
        assertEquals(2, dispatcher.observerCount());
        dispatcher.shutdown();
    }

    @Test
    void ignoraEventosNulosYNoDuplicaObservadores() {
        NotificationDispatcher dispatcher = new NotificationDispatcher(List.of());
        dispatcher.publish(null);
        dispatcher.subscribe(null);

        assertEquals(0, dispatcher.observerCount());
        dispatcher.shutdown();
    }

    private NotificationObserver observer(String name, CountDownLatch latch, AtomicInteger counter) {
        return new NotificationObserver() {
            @Override
            public void onEvent(DomainEvent event) {
                counter.incrementAndGet();
                latch.countDown();
            }

            @Override
            public String observerName() {
                return name;
            }
        };
    }
}
