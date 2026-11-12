package co.ufps.studymatch.application.group;

import co.ufps.studymatch.application.common.SubjectNotFoundException;
import co.ufps.studymatch.group.GroupModality;
import co.ufps.studymatch.group.IStudyGroupRepository;
import co.ufps.studymatch.group.StudyGroup;
import co.ufps.studymatch.shared.DomainEventPublisher;
import co.ufps.studymatch.shared.events.GroupCreatedEvent;
import co.ufps.studymatch.student.IStudentRepository;
import co.ufps.studymatch.student.Student;
import co.ufps.studymatch.student.StudentId;
import co.ufps.studymatch.student.StudentNotFoundException;
import co.ufps.studymatch.subject.ISubjectRepository;
import co.ufps.studymatch.subject.SubjectId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CreateGroupHandler {
    private final IStudyGroupRepository groupRepository;
    private final IStudentRepository studentRepository;
    private final ISubjectRepository subjectRepository;
    private final DomainEventPublisher eventPublisher;
    private final GroupDtoAssembler groupAssembler;

    @Inject
    public CreateGroupHandler(IStudyGroupRepository groupRepository,
                              IStudentRepository studentRepository,
                              ISubjectRepository subjectRepository,
                              DomainEventPublisher eventPublisher,
                              GroupDtoAssembler groupAssembler) {
        this.groupRepository = groupRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.eventPublisher = eventPublisher;
        this.groupAssembler = groupAssembler;
    }

    public StudyGroupDetailDTO execute(CreateGroupCommand command) {
        SubjectId subjectId = SubjectId.of(command.subjectId());
        subjectRepository.buscarId(subjectId)
                .orElseThrow(() -> new SubjectNotFoundException(command.subjectId()));
        StudentId creatorId = StudentId.of(command.creatorId());
        Student creator = studentRepository.buscarId(creatorId)
                .orElseThrow(() -> new StudentNotFoundException(command.creatorId()));
        StudyGroup group = StudyGroup.create(
                subjectId,
                command.name(),
                command.description(),
                creatorId,
                command.maxMembers(),
                command.schedule().stream().map(input -> input.toDomain()).toList(),
                GroupModality.fromString(command.modality())
        );
        creator.joinGroup(group.getGroupId());
        StudyGroup saved = groupRepository.guardar(group);
        eventPublisher.publish(new GroupCreatedEvent(creatorId,
                "Creaste el grupo " + saved.getName() + " para " + saved.getSubjectId().value(),
                saved.getGroupId().value()));
        return groupAssembler.toDetail(saved, creator);
    }
}
