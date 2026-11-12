package co.ufps.studymatch.application.student;

import co.ufps.studymatch.application.common.SubjectResolver;
import co.ufps.studymatch.student.IStudentRepository;
import co.ufps.studymatch.student.Student;
import co.ufps.studymatch.student.StudentId;
import co.ufps.studymatch.student.StudentNotFoundException;
import co.ufps.studymatch.subject.ISubjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class UpdateProfileHandler {
    private final IStudentRepository studentRepository;
    private final ISubjectRepository subjectRepository;
    private final StudentDtoAssembler studentAssembler;

    @Inject
    public UpdateProfileHandler(IStudentRepository studentRepository,
                                ISubjectRepository subjectRepository,
                                StudentDtoAssembler studentAssembler) {
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.studentAssembler = studentAssembler;
    }

    public StudentDTO execute(UpdateProfileCommand command) {
        StudentId studentId = StudentId.of(command.studentId());
        Student student = studentRepository.buscarId(studentId)
                .orElseThrow(() -> new StudentNotFoundException(command.studentId()));
        var subjects = SubjectResolver.resolve(subjectRepository, command.subjectIds());
        student.updateProfile(command.career(), command.semester(), command.bio(), subjects);
        return studentAssembler.toDto(studentRepository.guardar(student));
    }
}
