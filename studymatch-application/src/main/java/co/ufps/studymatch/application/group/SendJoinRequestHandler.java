package co.ufps.studymatch.application.group;

import co.ufps.studymatch.application.action.CommandInvoker;
import co.ufps.studymatch.application.action.commands.SendJoinRequestAction;
import co.ufps.studymatch.group.GroupNotFoundException;
import co.ufps.studymatch.group.IStudyGroupRepository;
import co.ufps.studymatch.group.JoinRequest;
import co.ufps.studymatch.group.JoinRequestNotFoundException;
import co.ufps.studymatch.group.StudyGroup;
import co.ufps.studymatch.group.StudyGroupId;
import co.ufps.studymatch.shared.DomainEventPublisher;
import co.ufps.studymatch.student.IStudentRepository;
import co.ufps.studymatch.student.StudentId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SendJoinRequestHandler {
    private final IStudyGroupRepository groupRepository;
    private final IStudentRepository studentRepository;
    private final DomainEventPublisher eventPublisher;
    private final CommandInvoker commandInvoker;
    private final GroupDtoAssembler groupAssembler;

    @Inject
    public SendJoinRequestHandler(IStudyGroupRepository groupRepository,
                                  IStudentRepository studentRepository,
                                  DomainEventPublisher eventPublisher,
                                  CommandInvoker commandInvoker,
                                  GroupDtoAssembler groupAssembler) {
        this.groupRepository = groupRepository;
        this.studentRepository = studentRepository;
        this.eventPublisher = eventPublisher;
        this.commandInvoker = commandInvoker;
        this.groupAssembler = groupAssembler;
    }

    public JoinRequestDTO execute(SendJoinRequestCommand command) {
        StudentId studentId = StudentId.of(command.studentId());
        StudyGroupId groupId = StudyGroupId.of(command.groupId());
        SendJoinRequestAction action = new SendJoinRequestAction(groupRepository, studentRepository,
                eventPublisher, studentId, groupId, command.message());
        action.execute();
        commandInvoker.registrar(action);
        return groupAssembler.toRequestDto(action.getRequest());
    }
}
