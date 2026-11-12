package co.ufps.studymatch.group;

import co.ufps.studymatch.student.StudentId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad interna que relaciona un estudiante con un grupo y su rol.
 */
public class GroupMembership {
    private final String membershipId;
    private final StudyGroupId groupId;
    private final StudentId studentId;
    private GroupMemberRole role;
    private final LocalDateTime joinedAt;

    private GroupMembership(String membershipId, StudyGroupId groupId, StudentId studentId,
                            GroupMemberRole role, LocalDateTime joinedAt) {
        if (membershipId == null || membershipId.isBlank()) {
            throw new IllegalArgumentException("El membershipId es obligatorio");
        }
        this.membershipId = membershipId;
        this.groupId = Objects.requireNonNull(groupId, "El groupId es obligatorio");
        this.studentId = Objects.requireNonNull(studentId, "El studentId es obligatorio");
        this.role = Objects.requireNonNull(role, "El rol es obligatorio");
        this.joinedAt = joinedAt != null ? joinedAt : LocalDateTime.now();
    }

    public static GroupMembership create(StudyGroupId groupId, StudentId studentId, GroupMemberRole role) {
        return new GroupMembership(java.util.UUID.randomUUID().toString(), groupId, studentId, role,
                LocalDateTime.now());
    }

    public static GroupMembership of(String membershipId, StudyGroupId groupId, StudentId studentId,
                                     GroupMemberRole role, LocalDateTime joinedAt) {
        return new GroupMembership(membershipId, groupId, studentId, role, joinedAt);
    }

    public void promoteToLeader() {
        this.role = GroupMemberRole.LEADER;
    }

    public void demoteToMember() {
        this.role = GroupMemberRole.MEMBER;
    }

    public boolean isLeader() {
        return role == GroupMemberRole.LEADER;
    }

    public String getMembershipId() {
        return membershipId;
    }

    public StudyGroupId getGroupId() {
        return groupId;
    }

    public StudentId getStudentId() {
        return studentId;
    }

    public GroupMemberRole getRole() {
        return role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
}
