package co.ufps.studymatch.application.group;

import co.ufps.studymatch.application.student.AvailabilitySlotDTO;
import co.ufps.studymatch.application.subject.SubjectDTO;

import java.time.LocalDateTime;
import java.util.List;

public record StudyGroupSummaryDTO(
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
        Double affinityScore,
        List<String> reasons
) {
}
