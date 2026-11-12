package co.ufps.studymatch.group;

import co.ufps.studymatch.ValidationException;

import java.util.Objects;
import java.util.UUID;

public final class StudyGroupId {
    private final String value;

    private StudyGroupId(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("El groupId no puede estar vacio");
        }
        this.value = value.strip();
    }

    public static StudyGroupId generate() {
        return new StudyGroupId(UUID.randomUUID().toString());
    }

    public static StudyGroupId of(String value) {
        return new StudyGroupId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StudyGroupId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
