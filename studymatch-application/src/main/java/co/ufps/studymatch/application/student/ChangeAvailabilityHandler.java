package co.ufps.studymatch.application.student;

import co.ufps.studymatch.application.action.CommandInvoker;
import co.ufps.studymatch.application.action.commands.ChangeAvailabilityAction;
import co.ufps.studymatch.shared.DomainEventPublisher;
import co.ufps.studymatch.student.AvailabilityStatus;
import co.ufps.studymatch.student.IStudentRepository;
import co.ufps.studymatch.student.Student;
import co.ufps.studymatch.student.StudentId;
import co.ufps.studymatch.student.StudentNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ChangeAvailabilityHandler {
    private final IStudentRepository studentRepository;
    private final DomainEventPublisher eventPublisher;
    private final CommandInvoker commandInvoker;
    private final StudentDtoAssembler studentAssembler;

    @Inject
    public ChangeAvailabilityHandler(IStudentRepository studentRepository,
                                     DomainEventPublisher eventPublisher,
                                     CommandInvoker commandInvoker,
                                     StudentDtoAssembler studentAssembler) {
        this.studentRepository = studentRepository;
        this.eventPublisher = eventPublisher;
        this.commandInvoker = commandInvoker;
        this.studentAssembler = studentAssembler;
    }

    public StudentDTO execute(ChangeAvailabilityCommand command) {
        StudentId studentId = StudentId.of(command.studentId());
        Student student = studentRepository.buscarId(studentId)
                .orElseThrow(() -> new StudentNotFoundException(command.studentId()));
        ChangeAvailabilityAction action = new ChangeAvailabilityAction(
                studentRepository,
                eventPublisher,
                studentId,
                AvailabilityStatus.fromString(command.availabilityStatus()),
                command.availabilitySlots().stream().map(input -> input.toDomain()).toList()
        );
        action.execute();
        commandInvoker.registrar(action);
        Student updated = action.getStudent();
        return studentAssembler.toDto(updated);
    }
}
