package co.ufps.studymatch.student;

import co.ufps.studymatch.ValidationException;

import java.util.Objects;
import java.util.UUID;

public final class StudentId {
    private final String value;

    private StudentId(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("El studentId no puede estar vacio");
        }
        this.value = value.strip();
    }

    public static StudentId generate() {
        return new StudentId(UUID.randomUUID().toString());
    }

    public static StudentId of(String value) {
        return new StudentId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StudentId studentId)) return false;
        return value.equals(studentId.value);
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
