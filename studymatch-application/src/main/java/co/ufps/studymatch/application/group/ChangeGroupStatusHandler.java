package co.ufps.studymatch.application.group;

import co.ufps.studymatch.ValidationException;
import co.ufps.studymatch.group.GroupNotFoundException;
import co.ufps.studymatch.group.GroupStatus;
import co.ufps.studymatch.group.IStudyGroupRepository;
import co.ufps.studymatch.group.StudyGroup;
import co.ufps.studymatch.group.StudyGroupId;
import co.ufps.studymatch.student.IStudentRepository;
import co.ufps.studymatch.student.Student;
import co.ufps.studymatch.student.StudentId;
import co.ufps.studymatch.student.StudentNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ChangeGroupStatusHandler {
    private final IStudyGroupRepository groupRepository;
    private final IStudentRepository studentRepository;
    private final GroupDtoAssembler groupAssembler;

    @Inject
    public ChangeGroupStatusHandler(IStudyGroupRepository groupRepository,
                                    IStudentRepository studentRepository,
                                    GroupDtoAssembler groupAssembler) {
        this.groupRepository = groupRepository;
        this.studentRepository = studentRepository;
        this.groupAssembler = groupAssembler;
    }

    public StudyGroupDetailDTO execute(ChangeGroupStatusCommand command) {
        GroupStatus target = parseStatus(command.status());
        StudyGroupId groupId = StudyGroupId.of(command.groupId());
        StudyGroup group = groupRepository.buscarId(groupId)
                .orElseThrow(() -> new GroupNotFoundException(command.groupId()));
        StudentId actorId = StudentId.of(command.actorId());
        group.changeStatus(actorId, target);
        StudyGroup saved = groupRepository.guardar(group);
        Student actor = studentRepository.buscarId(actorId)
                .orElseThrow(() -> new StudentNotFoundException(command.actorId()));
        return groupAssembler.toDetail(saved, actor);
    }

    private GroupStatus parseStatus(String raw) {
        try {
            return GroupStatus.valueOf(raw.strip().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Estado de grupo invalido: " + raw);
        }
    }
}
