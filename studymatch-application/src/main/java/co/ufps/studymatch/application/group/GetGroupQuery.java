package co.ufps.studymatch.application.group;

import co.ufps.studymatch.group.GroupNotFoundException;
import co.ufps.studymatch.group.IStudyGroupRepository;
import co.ufps.studymatch.group.StudyGroup;
import co.ufps.studymatch.group.StudyGroupId;
import co.ufps.studymatch.student.IStudentRepository;
import co.ufps.studymatch.student.Student;
import co.ufps.studymatch.student.StudentId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GetGroupQuery {
    private final IStudyGroupRepository groupRepository;
    private final IStudentRepository studentRepository;
    private final GroupDtoAssembler groupAssembler;

    @Inject
    public GetGroupQuery(IStudyGroupRepository groupRepository,
                         IStudentRepository studentRepository,
                         GroupDtoAssembler groupAssembler) {
        this.groupRepository = groupRepository;
        this.studentRepository = studentRepository;
        this.groupAssembler = groupAssembler;
    }

    public StudyGroupDetailDTO execute(String groupId, String viewerId) {
        StudyGroup group = groupRepository.buscarId(StudyGroupId.of(groupId))
                .orElseThrow(() -> new GroupNotFoundException(groupId));
        Student viewer = viewerId == null || viewerId.isBlank()
                ? null
                : studentRepository.buscarId(StudentId.of(viewerId)).orElse(null);
        return groupAssembler.toDetail(group, viewer);
    }
}
