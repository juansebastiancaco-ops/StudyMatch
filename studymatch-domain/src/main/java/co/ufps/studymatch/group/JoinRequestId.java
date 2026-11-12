package co.ufps.studymatch.group;

import co.ufps.studymatch.ValidationException;

import java.util.Objects;
import java.util.UUID;

public final class JoinRequestId {
    private final String value;

    private JoinRequestId(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("El requestId no puede estar vacio");
        }
        this.value = value.strip();
    }

    public static JoinRequestId generate() {
        return new JoinRequestId(UUID.randomUUID().toString());
    }

    public static JoinRequestId of(String value) {
        return new JoinRequestId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JoinRequestId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
