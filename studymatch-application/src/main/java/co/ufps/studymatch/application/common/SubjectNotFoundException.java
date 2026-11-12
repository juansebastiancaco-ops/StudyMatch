package co.ufps.studymatch.application.common;

import co.ufps.studymatch.NotFoundException;

public class SubjectNotFoundException extends NotFoundException {
    public SubjectNotFoundException(String subjectId) {
        super("Asignatura no encontrada con id: " + subjectId);
    }
}
