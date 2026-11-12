package co.ufps.studymatch.application.student;

import co.ufps.studymatch.application.subject.SubjectDTO;

import java.util.List;

public record AcademicProfileDTO(
        String profileId,
        String bio,
        List<SubjectDTO> subjects,
        String availabilityStatus,
        List<AvailabilitySlotDTO> availabilitySlots
) {
}
