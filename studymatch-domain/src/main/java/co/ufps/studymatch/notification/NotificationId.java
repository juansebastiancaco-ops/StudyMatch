package co.ufps.studymatch.notification;

import co.ufps.studymatch.ValidationException;

import java.util.Objects;
import java.util.UUID;

public final class NotificationId {
    private final String value;

    private NotificationId(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("El notificationId no puede estar vacio");
        }
        this.value = value.strip();
    }

    public static NotificationId generate() {
        return new NotificationId(UUID.randomUUID().toString());
    }

    public static NotificationId of(String value) {
        return new NotificationId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
