package co.ufps.studymatch.application.group;

import co.ufps.studymatch.group.JoinRequest;
import co.ufps.studymatch.student.Student;

import java.time.LocalDateTime;

public record JoinRequestDTO(
        String requestId,
        String groupId,
        String studentId,
        String studentName,
        String studentCareer,
        int studentSemester,
        String message,
        String status,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {
    public static JoinRequestDTO from(JoinRequest request, Student student) {
        return new JoinRequestDTO(
                request.getRequestId().value(),
                request.getGroupId().value(),
                request.getStudentId().value(),
                student != null ? student.getFullName() : "Estudiante",
                student != null ? student.getCareer() : "",
                student != null ? student.getSemester() : 0,
                request.getMessage(),
                request.getStatus().name(),
                request.getCreatedAt(),
                request.getResolvedAt()
        );
    }
}
