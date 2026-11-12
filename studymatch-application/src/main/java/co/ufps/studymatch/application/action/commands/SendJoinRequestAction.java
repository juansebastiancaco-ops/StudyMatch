package co.ufps.studymatch.application.action.commands;

import co.ufps.studymatch.application.action.ReversibleCommand;
import co.ufps.studymatch.group.GroupNotFoundException;
import co.ufps.studymatch.group.IStudyGroupRepository;
import co.ufps.studymatch.group.JoinRequest;
import co.ufps.studymatch.group.JoinRequestId;
import co.ufps.studymatch.group.StudyGroup;
import co.ufps.studymatch.group.StudyGroupId;
import co.ufps.studymatch.shared.DomainEventPublisher;
import co.ufps.studymatch.shared.events.JoinRequestSentEvent;
import co.ufps.studymatch.student.IStudentRepository;
import co.ufps.studymatch.student.Student;
import co.ufps.studymatch.student.StudentId;
import co.ufps.studymatch.student.StudentNotFoundException;

import java.util.UUID;

public class SendJoinRequestAction implements ReversibleCommand {
    private final IStudyGroupRepository groupRepository;
    private final IStudentRepository studentRepository;
    private final DomainEventPublisher eventPublisher;
    private final String actionId;
    private final StudentId studentId;
    private final StudyGroupId groupId;
    private final String message;

    private JoinRequestId requestId;
    private JoinRequest request;
    private String groupName;

    public SendJoinRequestAction(IStudyGroupRepository groupRepository,
                                 IStudentRepository studentRepository,
                                 DomainEventPublisher eventPublisher,
                                 StudentId studentId,
                                 StudyGroupId groupId,
                                 String message) {
        this.groupRepository = groupRepository;
        this.studentRepository = studentRepository;
        this.eventPublisher = eventPublisher;
        this.actionId = UUID.randomUUID().toString();
        this.studentId = studentId;
        this.groupId = groupId;
        this.message = message;
    }

    @Override
    public void execute() {
        Student student = studentRepository.buscarId(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId.value()));
        StudyGroup group = groupRepository.buscarId(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId.value()));
        JoinRequest request = group.sendJoinRequest(studentId, message,
                student.hasScheduleOverlapWith(group.getSchedule()));
        groupRepository.guardar(group);
        this.request = request;
        this.requestId = request.getRequestId();
        this.groupName = group.getName();
        StudentId leaderId = group.getLeaderId().orElse(group.getCreatorId());
        eventPublisher.publish(new JoinRequestSentEvent(leaderId,
                student.getFullName() + " solicito unirse al grupo " + group.getName(),
                requestId.value()));
    }

    @Override
    public void undo() {
        StudyGroup group = groupRepository.buscarId(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId.value()));
        group.cancelRequest(requestId, studentId);
        groupRepository.guardar(group);
    }

    public JoinRequestId getRequestId() {
        return requestId;
    }

    public JoinRequest getRequest() {
        return request;
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
        return "SEND_JOIN_REQUEST";
    }

    @Override
    public String description() {
        return groupName == null ? "Enviar solicitud de ingreso" : "Enviar solicitud al grupo " + groupName;
    }
}
