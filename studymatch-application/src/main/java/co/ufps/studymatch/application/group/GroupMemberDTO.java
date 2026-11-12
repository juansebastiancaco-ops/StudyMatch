package co.ufps.studymatch.application.group;

import co.ufps.studymatch.group.GroupMembership;
import co.ufps.studymatch.student.Student;

import java.time.LocalDateTime;

public record GroupMemberDTO(
        String membershipId,
        String studentId,
        String fullName,
        String career,
        int semester,
        String role,
        LocalDateTime joinedAt
) {
    public static GroupMemberDTO from(GroupMembership membership, Student student) {
        return new GroupMemberDTO(
                membership.getMembershipId(),
                membership.getStudentId().value(),
                student != null ? student.getFullName() : "Estudiante",
                student != null ? student.getCareer() : "",
                student != null ? student.getSemester() : 0,
                membership.getRole().name(),
                membership.getJoinedAt()
        );
    }
}
