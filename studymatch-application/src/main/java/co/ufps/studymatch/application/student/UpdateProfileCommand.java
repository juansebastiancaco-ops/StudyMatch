package co.ufps.studymatch.application.student;

import co.ufps.studymatch.ValidationException;

import java.util.List;

public record UpdateProfileCommand(
        String studentId,
        String career,
        int semester,
        String bio,
        List<String> subjectIds
) {
    public UpdateProfileCommand {
        if (studentId == null || studentId.isBlank()) {
            throw new ValidationException("El studentId es obligatorio");
        }
        if (career == null || career.isBlank()) {
            throw new ValidationException("La carrera es obligatoria");
        }
        if (semester < 1 || semester > 12) {
            throw new ValidationException("El semestre debe estar entre 1 y 12");
        }
        subjectIds = subjectIds == null ? List.of() : List.copyOf(subjectIds);
    }
}
