package co.ufps.studymatch.application.student;

import co.ufps.studymatch.application.group.StudyGroupSummaryDTO;

import java.util.List;

public record DashboardDTO(
        StudentDTO student,
        List<StudyGroupSummaryDTO> activeGroups,
        List<StudyGroupSummaryDTO> recommendedGroups,
        List<StudentSummaryDTO> recommendedStudents,
        long unreadNotifications,
        long pendingJoinRequests
) {
}
