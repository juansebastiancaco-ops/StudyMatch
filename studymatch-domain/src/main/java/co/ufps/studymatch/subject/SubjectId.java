package co.ufps.studymatch.subject;

import co.ufps.studymatch.ValidationException;

import java.util.Objects;
import java.util.UUID;

public final class SubjectId {
    private final String value;

    private SubjectId(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("El subjectId no puede estar vacio");
        }
        this.value = value.strip();
    }

    public static SubjectId generate() {
        return new SubjectId(UUID.randomUUID().toString());
    }

    public static SubjectId of(String value) {
        return new SubjectId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubjectId subjectId)) return false;
        return value.equals(subjectId.value);
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
