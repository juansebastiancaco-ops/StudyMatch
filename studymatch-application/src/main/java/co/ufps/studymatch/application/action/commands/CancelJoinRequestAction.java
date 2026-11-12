package co.ufps.studymatch.application.action.commands;

import co.ufps.studymatch.application.action.ReversibleCommand;
import co.ufps.studymatch.group.GroupNotFoundException;
import co.ufps.studymatch.group.IStudyGroupRepository;
import co.ufps.studymatch.group.JoinRequest;
import co.ufps.studymatch.group.JoinRequestId;
import co.ufps.studymatch.group.StudyGroup;
import co.ufps.studymatch.group.StudyGroupId;
import co.ufps.studymatch.student.StudentId;

import java.util.UUID;

public class CancelJoinRequestAction implements ReversibleCommand {
    private final IStudyGroupRepository groupRepository;
    private final String actionId;
    private final StudentId studentId;
    private final StudyGroupId groupId;
    private final JoinRequestId requestId;
    private String groupName;
    private JoinRequest request;

    public CancelJoinRequestAction(IStudyGroupRepository groupRepository,
                                   StudentId studentId,
                                   StudyGroupId groupId,
                                   JoinRequestId requestId) {
        this.groupRepository = groupRepository;
        this.actionId = UUID.randomUUID().toString();
        this.studentId = studentId;
        this.groupId = groupId;
        this.requestId = requestId;
    }

    @Override
    public void execute() {
        StudyGroup group = groupRepository.buscarId(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId.value()));
        this.request = group.cancelRequest(requestId, studentId);
        groupRepository.guardar(group);
        this.groupName = group.getName();
    }

    public JoinRequest getRequest() {
        return request;
    }

    @Override
    public void undo() {
        StudyGroup group = groupRepository.buscarId(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId.value()));
        group.revertCancelledRequest(requestId);
        groupRepository.guardar(group);
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
        return "CANCEL_JOIN_REQUEST";
    }

    @Override
    public String description() {
        return groupName == null ? "Cancelar solicitud de ingreso" : "Cancelar solicitud al grupo " + groupName;
    }
}
