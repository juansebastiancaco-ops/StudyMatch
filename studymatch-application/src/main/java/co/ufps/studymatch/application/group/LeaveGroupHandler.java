package co.ufps.studymatch.application.group;

import co.ufps.studymatch.application.action.CommandInvoker;
import co.ufps.studymatch.application.action.commands.LeaveGroupAction;
import co.ufps.studymatch.group.IStudyGroupRepository;
import co.ufps.studymatch.group.StudyGroupId;
import co.ufps.studymatch.shared.DomainEventPublisher;
import co.ufps.studymatch.student.IStudentRepository;
import co.ufps.studymatch.student.StudentId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class LeaveGroupHandler {
    private final IStudyGroupRepository groupRepository;
    private final IStudentRepository studentRepository;
    private final DomainEventPublisher eventPublisher;
    private final CommandInvoker commandInvoker;

    @Inject
    public LeaveGroupHandler(IStudyGroupRepository groupRepository,
                             IStudentRepository studentRepository,
                             DomainEventPublisher eventPublisher,
                             CommandInvoker commandInvoker) {
        this.groupRepository = groupRepository;
        this.studentRepository = studentRepository;
        this.eventPublisher = eventPublisher;
        this.commandInvoker = commandInvoker;
    }

    public void execute(LeaveGroupCommand command) {
        LeaveGroupAction action = new LeaveGroupAction(groupRepository, studentRepository, eventPublisher,
                StudentId.of(command.studentId()), StudyGroupId.of(command.groupId()));
        action.execute();
        commandInvoker.registrar(action);
    }
}
