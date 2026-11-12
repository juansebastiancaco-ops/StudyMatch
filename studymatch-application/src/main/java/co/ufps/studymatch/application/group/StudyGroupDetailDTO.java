package co.ufps.studymatch.application.group;

import co.ufps.studymatch.application.student.AvailabilitySlotDTO;
import co.ufps.studymatch.application.subject.SubjectDTO;

import java.time.LocalDateTime;
import java.util.List;

public record StudyGroupDetailDTO(
        String groupId,
        SubjectDTO subject,
        String name,
        String description,
        String creatorId,
        String creatorName,
        int maxMembers,
        int memberCount,
        int availableSpots,
        String modality,
        String status,
        LocalDateTime createdAt,
        List<AvailabilitySlotDTO> schedule,
        List<GroupMemberDTO> members,
        List<JoinRequestDTO> requests,
        String viewerRole
) {
    public static StudyGroupDetailDTO from(StudyGroupSummaryDTO summary,
                                           List<GroupMemberDTO> members,
                                           List<JoinRequestDTO> requests,
                                           String viewerRole) {
        return new StudyGroupDetailDTO(
                summary.groupId(),
                summary.subject(),
                summary.name(),
                summary.description(),
                summary.creatorId(),
                summary.creatorName(),
                summary.maxMembers(),
                summary.memberCount(),
                summary.availableSpots(),
                summary.modality(),
                summary.status(),
                summary.createdAt(),
                summary.schedule(),
                List.copyOf(members),
                List.copyOf(requests),
                viewerRole
        );
    }
}
