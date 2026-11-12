package co.ufps.studymatch.application.group;

import co.ufps.studymatch.group.GroupNotFoundException;
import co.ufps.studymatch.group.IStudyGroupRepository;
import co.ufps.studymatch.group.NotGroupLeaderException;
import co.ufps.studymatch.group.StudyGroup;
import co.ufps.studymatch.group.StudyGroupId;
import co.ufps.studymatch.student.StudentId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class GetGroupRequestsQuery {
    private final IStudyGroupRepository groupRepository;
    private final GroupDtoAssembler groupAssembler;

    @Inject
    public GetGroupRequestsQuery(IStudyGroupRepository groupRepository, GroupDtoAssembler groupAssembler) {
        this.groupRepository = groupRepository;
        this.groupAssembler = groupAssembler;
    }

    public List<JoinRequestDTO> execute(String groupId, String viewerId) {
        StudyGroup group = groupRepository.buscarId(StudyGroupId.of(groupId))
                .orElseThrow(() -> new GroupNotFoundException(groupId));
        StudentId viewer = StudentId.of(viewerId);
        if (!group.isLeader(viewer)) {
            throw new NotGroupLeaderException();
        }
        return groupAssembler.toRequestDtos(group.getRequests());
    }
}
