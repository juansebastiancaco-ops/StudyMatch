package co.ufps.studymatch.application.action.commands;

import co.ufps.studymatch.application.action.ReversibleCommand;
import co.ufps.studymatch.group.GroupMembership;
import co.ufps.studymatch.group.GroupNotFoundException;
import co.ufps.studymatch.group.IStudyGroupRepository;
import co.ufps.studymatch.group.StudyGroup;
import co.ufps.studymatch.group.StudyGroupId;
import co.ufps.studymatch.shared.DomainEventPublisher;
import co.ufps.studymatch.shared.events.MemberLeftGroupEvent;
import co.ufps.studymatch.student.IStudentRepository;
import co.ufps.studymatch.student.Student;
import co.ufps.studymatch.student.StudentId;
import co.ufps.studymatch.student.StudentNotFoundException;

import java.util.UUID;

public class LeaveGroupAction implements ReversibleCommand {
    private final IStudyGroupRepository groupRepository;
    private final IStudentRepository studentRepository;
    private final DomainEventPublisher eventPublisher;
    private final String actionId;
    private final StudentId studentId;
    private final StudyGroupId groupId;
    private GroupMembership removedMembership;
    private String groupName;

    public LeaveGroupAction(IStudyGroupRepository groupRepository,
                            IStudentRepository studentRepository,
                            DomainEventPublisher eventPublisher,
                            StudentId studentId,
                            StudyGroupId groupId) {
        this.groupRepository = groupRepository;
        this.studentRepository = studentRepository;
        this.eventPublisher = eventPublisher;
        this.actionId = UUID.randomUUID().toString();
        this.studentId = studentId;
        this.groupId = groupId;
    }

    @Override
    public void execute() {
        Student student = studentRepository.buscarId(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId.value()));
        StudyGroup group = groupRepository.buscarId(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId.value()));
        this.removedMembership = group.leaveGroup(studentId);
        groupRepository.guardar(group);
        this.groupName = group.getName();
        StudentId recipient = group.getLeaderId().orElse(group.getCreatorId());
        eventPublisher.publish(new MemberLeftGroupEvent(recipient,
                student.getFullName() + " salio del grupo " + group.getName(),
                groupId.value()));
    }

    @Override
    public void undo() {
        StudyGroup group = groupRepository.buscarId(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId.value()));
        group.restoreMembership(removedMembership);
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
        return "LEAVE_GROUP";
    }

    @Override
    public String description() {
        return groupName == null ? "Salir de un grupo" : "Salir del grupo " + groupName;
    }
}
