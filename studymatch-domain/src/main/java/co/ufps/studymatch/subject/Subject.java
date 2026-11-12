package co.ufps.studymatch.subject;

import co.ufps.studymatch.ValidationException;

import java.util.Objects;

/**
 * Catalogo academico de asignaturas. No es un agregado de negocio complejo: es referencia.
 */
public class Subject {
    private final SubjectId subjectId;
    private final String code;
    private final String name;
    private final String area;
    private final int credits;
    private final int semester;
    private final boolean active;

    private Subject(SubjectId subjectId, String code, String name, String area, int credits,
                    int semester, boolean active) {
        this.subjectId = Objects.requireNonNull(subjectId, "El subjectId es obligatorio");
        this.code = requireText(code, "El codigo de la asignatura es obligatorio").toUpperCase();
        this.name = requireText(name, "El nombre de la asignatura es obligatorio");
        this.area = requireText(area, "El area de la asignatura es obligatoria");
        if (credits < 0 || credits > 20) {
            throw new ValidationException("Los creditos deben estar entre 0 y 20");
        }
        if (semester < 1 || semester > 12) {
            throw new ValidationException("El semestre de la asignatura debe estar entre 1 y 12");
        }
        this.credits = credits;
        this.semester = semester;
        this.active = active;
    }

    public static Subject create(String code, String name, String area, int credits, int semester) {
        return new Subject(SubjectId.generate(), code, name, area, credits, semester, true);
    }

    public static Subject of(SubjectId subjectId, String code, String name, String area, int credits,
                             int semester, boolean active) {
        return new Subject(subjectId, code, name, area, credits, semester, active);
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
        return value.strip();
    }

    public SubjectId getSubjectId() {
        return subjectId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getArea() {
        return area;
    }

    public int getCredits() {
        return credits;
    }

    public int getSemester() {
        return semester;
    }

    public boolean isActive() {
        return active;
    }
}
