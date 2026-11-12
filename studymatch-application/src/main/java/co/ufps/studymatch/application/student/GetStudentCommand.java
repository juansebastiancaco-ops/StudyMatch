package co.ufps.studymatch.application.student;

import co.ufps.studymatch.student.StudentId;

public record GetStudentCommand(String studentId) {
    public StudentId toStudentId() {
        return StudentId.of(studentId);
    }
}
