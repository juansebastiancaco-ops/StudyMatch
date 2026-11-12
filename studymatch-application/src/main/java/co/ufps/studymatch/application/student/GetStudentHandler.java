package co.ufps.studymatch.application.student;

import co.ufps.studymatch.student.IStudentRepository;
import co.ufps.studymatch.student.Student;
import co.ufps.studymatch.student.StudentNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GetStudentHandler {
    private final IStudentRepository studentRepository;
    private final StudentDtoAssembler studentAssembler;

    @Inject
    public GetStudentHandler(IStudentRepository studentRepository, StudentDtoAssembler studentAssembler) {
        this.studentRepository = studentRepository;
        this.studentAssembler = studentAssembler;
    }

    public StudentDTO execute(GetStudentCommand command) {
        Student student = studentRepository.buscarId(command.toStudentId())
                .orElseThrow(() -> new StudentNotFoundException(command.studentId()));
        return studentAssembler.toDto(student);
    }
}
