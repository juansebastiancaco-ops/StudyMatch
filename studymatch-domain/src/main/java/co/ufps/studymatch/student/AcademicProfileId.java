package co.ufps.studymatch.student;

import co.ufps.studymatch.ValidationException;

import java.util.Objects;
import java.util.UUID;

public final class AcademicProfileId {
    private final String value;

    private AcademicProfileId(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("El academicProfileId no puede estar vacio");
        }
        this.value = value.strip();
    }

    public static AcademicProfileId generate() {
        return new AcademicProfileId(UUID.randomUUID().toString());
    }

    public static AcademicProfileId of(String value) {
        return new AcademicProfileId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AcademicProfileId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
