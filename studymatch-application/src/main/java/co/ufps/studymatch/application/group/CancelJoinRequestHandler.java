package co.ufps.studymatch.application.group;

import co.ufps.studymatch.application.action.CommandInvoker;
import co.ufps.studymatch.application.action.commands.CancelJoinRequestAction;
import co.ufps.studymatch.group.GroupNotFoundException;
import co.ufps.studymatch.group.IStudyGroupRepository;
import co.ufps.studymatch.group.JoinRequest;
import co.ufps.studymatch.group.JoinRequestId;
import co.ufps.studymatch.group.JoinRequestNotFoundException;
import co.ufps.studymatch.group.StudyGroup;
import co.ufps.studymatch.group.StudyGroupId;
import co.ufps.studymatch.student.StudentId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CancelJoinRequestHandler {
    private final IStudyGroupRepository groupRepository;
    private final CommandInvoker commandInvoker;
    private final GroupDtoAssembler groupAssembler;

    @Inject
    public CancelJoinRequestHandler(IStudyGroupRepository groupRepository,
                                    CommandInvoker commandInvoker,
                                    GroupDtoAssembler groupAssembler) {
        this.groupRepository = groupRepository;
        this.commandInvoker = commandInvoker;
        this.groupAssembler = groupAssembler;
    }

    public JoinRequestDTO execute(CancelJoinRequestCommand command) {
        StudentId studentId = StudentId.of(command.studentId());
        StudyGroupId groupId = StudyGroupId.of(command.groupId());
        JoinRequestId requestId = JoinRequestId.of(command.requestId());
        CancelJoinRequestAction action = new CancelJoinRequestAction(groupRepository, studentId, groupId, requestId);
        action.execute();
        commandInvoker.registrar(action);
        return groupAssembler.toRequestDto(action.getRequest());
    }
}
