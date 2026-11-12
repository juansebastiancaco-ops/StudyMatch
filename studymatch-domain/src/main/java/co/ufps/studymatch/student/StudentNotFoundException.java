package co.ufps.studymatch.student;

import co.ufps.studymatch.NotFoundException;

public class StudentNotFoundException extends NotFoundException {
    public StudentNotFoundException(String studentId) {
        super("Estudiante no encontrado con id: " + studentId);
    }
}
