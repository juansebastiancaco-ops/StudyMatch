package co.ufps.studymatch.application.student;

import java.time.LocalDateTime;

public record StudentDTO(
        String studentId,
        String email,
        String fullName,
        String career,
        int semester,
        String status,
        LocalDateTime createdAt,
        AcademicProfileDTO profile,
        StudentStatsDTO stats
) {
}
