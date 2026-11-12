package co.ufps.studymatch.application.auth;

import co.ufps.studymatch.application.common.SubjectResolver;
import co.ufps.studymatch.application.student.StudentDtoAssembler;
import co.ufps.studymatch.auth.PasswordHasher;
import co.ufps.studymatch.auth.TokenIssuer;
import co.ufps.studymatch.shared.DomainEventPublisher;
import co.ufps.studymatch.shared.events.StudentRegisteredEvent;
import co.ufps.studymatch.student.EmailAlreadyExistsException;
import co.ufps.studymatch.student.IStudentRepository;
import co.ufps.studymatch.student.Student;
import co.ufps.studymatch.subject.ISubjectRepository;
import co.ufps.studymatch.subject.SubjectId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class RegisterStudentHandler {
    private final IStudentRepository studentRepository;
    private final ISubjectRepository subjectRepository;
    private final PasswordHasher passwordHasher;
    private final DomainEventPublisher eventPublisher;
    private final TokenIssuer tokenIssuer;
    private final StudentDtoAssembler studentAssembler;

    @Inject
    public RegisterStudentHandler(IStudentRepository studentRepository,
                                  ISubjectRepository subjectRepository,
                                  PasswordHasher passwordHasher,
                                  DomainEventPublisher eventPublisher,
                                  TokenIssuer tokenIssuer,
                                  StudentDtoAssembler studentAssembler) {
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.passwordHasher = passwordHasher;
        this.eventPublisher = eventPublisher;
        this.tokenIssuer = tokenIssuer;
        this.studentAssembler = studentAssembler;
    }

    public AuthResponse execute(RegisterStudentCommand command) {
        if (studentRepository.existeEmail(command.email())) {
            throw new EmailAlreadyExistsException(command.email());
        }
        List<SubjectId> subjects = SubjectResolver.resolve(subjectRepository, command.subjectIds());
        String passwordHash = passwordHasher.hash(command.password());
        Student student = Student.register(
                command.email(),
                passwordHash,
                command.fullName(),
                command.career(),
                command.semester(),
                command.bio(),
                subjects,
                command.toAvailabilityStatus(),
                command.toAvailabilitySlots()
        );
        Student saved = studentRepository.guardar(student);
        eventPublisher.publish(new StudentRegisteredEvent(saved.getStudentId(),
                "Bienvenido a StudyMatch, " + saved.getFullName() + ". Completa tu perfil para mejorar tus matches."));
        var token = tokenIssuer.issue(saved.getStudentId(), saved.getEmail(), saved.getFullName());
        return new AuthResponse(token.token(), "Bearer", token.expiresAt(), studentAssembler.toDto(saved));
    }
}
