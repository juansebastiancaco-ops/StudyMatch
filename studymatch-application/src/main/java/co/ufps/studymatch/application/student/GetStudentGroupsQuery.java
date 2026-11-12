package co.ufps.studymatch.application.student;

import co.ufps.studymatch.application.group.GroupDtoAssembler;
import co.ufps.studymatch.application.group.StudyGroupSummaryDTO;
import co.ufps.studymatch.group.IStudyGroupRepository;
import co.ufps.studymatch.group.StudyGroup;
import co.ufps.studymatch.student.StudentId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class GetStudentGroupsQuery {
    private final IStudyGroupRepository groupRepository;
    private final GroupDtoAssembler groupAssembler;

    @Inject
    public GetStudentGroupsQuery(IStudyGroupRepository groupRepository, GroupDtoAssembler groupAssembler) {
        this.groupRepository = groupRepository;
        this.groupAssembler = groupAssembler;
    }

    public List<StudyGroupSummaryDTO> execute(String studentId) {
        List<StudyGroup> groups = groupRepository.buscarPorEstudiante(StudentId.of(studentId));
        return groups.stream()
                .map(group -> groupAssembler.toSummary(group, null, List.of()))
                .toList();
    }
}
