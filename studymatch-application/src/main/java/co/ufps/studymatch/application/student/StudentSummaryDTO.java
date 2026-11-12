package co.ufps.studymatch.application.student;

import co.ufps.studymatch.application.subject.SubjectDTO;

import java.util.List;

public record StudentSummaryDTO(
        String studentId,
        String fullName,
        String career,
        int semester,
        String bio,
        List<SubjectDTO> subjects,
        String availabilityStatus,
        List<AvailabilitySlotDTO> availabilitySlots,
        Double affinityScore,
        List<String> reasons,
        List<String> sharedSubjects
) {
}
