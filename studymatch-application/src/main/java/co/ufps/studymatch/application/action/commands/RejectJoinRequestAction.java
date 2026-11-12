package co.ufps.studymatch.application.action.commands;

import co.ufps.studymatch.application.action.ReversibleCommand;
import co.ufps.studymatch.group.GroupNotFoundException;
import co.ufps.studymatch.group.IStudyGroupRepository;
import co.ufps.studymatch.group.JoinRequest;
import co.ufps.studymatch.group.JoinRequestId;
import co.ufps.studymatch.group.JoinRequestNotFoundException;
import co.ufps.studymatch.group.StudyGroup;
import co.ufps.studymatch.group.StudyGroupId;
import co.ufps.studymatch.shared.DomainEventPublisher;
import co.ufps.studymatch.shared.events.JoinRequestRejectedEvent;
import co.ufps.studymatch.student.IStudentRepository;
import co.ufps.studymatch.student.Student;
import co.ufps.studymatch.student.StudentId;
import co.ufps.studymatch.student.StudentNotFoundException;

import java.util.UUID;

public class RejectJoinRequestAction implements ReversibleCommand {
    private final IStudyGroupRepository groupRepository;
    private final IStudentRepository studentRepository;
    private final DomainEventPublisher eventPublisher;
    private final String actionId;
    private final StudentId leaderId;
    private final StudyGroupId groupId;
    private final JoinRequestId requestId;

    private String groupName;
    private String studentName;
    private JoinRequest request;

    public RejectJoinRequestAction(IStudyGroupRepository groupRepository,
                                   IStudentRepository studentRepository,
                                   DomainEventPublisher eventPublisher,
                                   StudentId leaderId,
                                   StudyGroupId groupId,
                                   JoinRequestId requestId) {
        this.groupRepository = groupRepository;
        this.studentRepository = studentRepository;
        this.eventPublisher = eventPublisher;
        this.actionId = UUID.randomUUID().toString();
        this.leaderId = leaderId;
        this.groupId = groupId;
        this.requestId = requestId;
    }

    @Override
    public void execute() {
        StudyGroup group = groupRepository.buscarId(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId.value()));
        JoinRequest joinRequest = group.getRequests().stream()
                .filter(item -> item.getRequestId().equals(requestId))
                .findFirst()
                .orElseThrow(() -> new JoinRequestNotFoundException(requestId.value()));
        Student student = studentRepository.buscarId(joinRequest.getStudentId())
                .orElseThrow(() -> new StudentNotFoundException(joinRequest.getStudentId().value()));
        this.request = group.rejectRequest(requestId, leaderId);
        groupRepository.guardar(group);
        this.groupName = group.getName();
        this.studentName = student.getFullName();
        eventPublisher.publish(new JoinRequestRejectedEvent(student.getStudentId(),
                "Tu solicitud para el grupo " + group.getName() + " fue rechazada",
                requestId.value()));
    }

    public JoinRequest getRequest() {
        return request;
    }

    @Override
    public void undo() {
        StudyGroup group = groupRepository.buscarId(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId.value()));
        group.revertRejectedRequest(requestId);
        groupRepository.guardar(group);
    }

    @Override
    public String actionId() {
        return actionId;
    }

    @Override
    public StudentId studentId() {
        return leaderId;
    }

    @Override
    public String type() {
        return "REJECT_JOIN_REQUEST";
    }

    @Override
    public String description() {
        if (studentName == null) {
            return "Rechazar solicitud de ingreso";
        }
        return "Rechazar a " + studentName + " del grupo " + groupName;
    }
}
