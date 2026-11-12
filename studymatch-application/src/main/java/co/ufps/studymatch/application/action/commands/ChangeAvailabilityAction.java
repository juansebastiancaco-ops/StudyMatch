package co.ufps.studymatch.application.action.commands;

import co.ufps.studymatch.application.action.ReversibleCommand;
import co.ufps.studymatch.shared.DomainEventPublisher;
import co.ufps.studymatch.shared.events.AvailabilityChangedEvent;
import co.ufps.studymatch.student.AvailabilitySlot;
import co.ufps.studymatch.student.AvailabilityStatus;
import co.ufps.studymatch.student.IStudentRepository;
import co.ufps.studymatch.student.Student;
import co.ufps.studymatch.student.StudentId;
import co.ufps.studymatch.student.StudentNotFoundException;

import java.util.List;
import java.util.UUID;

public class ChangeAvailabilityAction implements ReversibleCommand {
    private final IStudentRepository studentRepository;
    private final DomainEventPublisher eventPublisher;
    private final String actionId;
    private final StudentId studentId;
    private final AvailabilityStatus target;
    private final List<AvailabilitySlot> newSlots;

    private AvailabilityStatus previousStatus;
    private List<AvailabilitySlot> previousSlots;
    private Student student;

    public ChangeAvailabilityAction(IStudentRepository studentRepository,
                                    DomainEventPublisher eventPublisher,
                                    StudentId studentId,
                                    AvailabilityStatus target,
                                    List<AvailabilitySlot> newSlots) {
        this.studentRepository = studentRepository;
        this.eventPublisher = eventPublisher;
        this.actionId = UUID.randomUUID().toString();
        this.studentId = studentId;
        this.target = target;
        this.newSlots = List.copyOf(newSlots);
    }

    @Override
    public void execute() {
        Student student = studentRepository.buscarId(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId.value()));
        this.previousStatus = student.getAvailabilityStatus();
        this.previousSlots = List.copyOf(student.getAvailabilitySlots());
        student.changeAvailability(target, newSlots);
        this.student = studentRepository.guardar(student);
        eventPublisher.publish(new AvailabilityChangedEvent(studentId,
                "Tu disponibilidad cambio a " + target.name()));
    }

    public Student getStudent() {
        return student;
    }

    @Override
    public void undo() {
        Student student = studentRepository.buscarId(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId.value()));
        student.restoreAvailability(previousStatus, previousSlots);
        studentRepository.guardar(student);
    }

    @Override
    public String actionId() {
        return actionId;
    }

    @Override
    public StudentId studentId() {
        return studentId;
    }

    @Override
    public String type() {
        return "CHANGE_AVAILABILITY";
    }

    @Override
    public String description() {
        return "Cambiar disponibilidad a " + target.name();
    }
}
