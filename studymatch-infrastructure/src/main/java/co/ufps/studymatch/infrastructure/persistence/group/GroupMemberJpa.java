package co.ufps.studymatch.infrastructure.persistence.group;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_members",
        uniqueConstraints = @UniqueConstraint(name = "uk_group_member", columnNames = {"group_id", "student_id"}),
        indexes = {
                @Index(name = "idx_group_members_group", columnList = "group_id"),
                @Index(name = "idx_group_members_student", columnList = "student_id")
        })
public class GroupMemberJpa {
    @Id
    @Column(name = "membership_id", length = 36, nullable = false)
    private String membershipId;

    @Column(name = "group_id", length = 36, nullable = false)
    private String groupId;

    @Column(name = "student_id", length = 36, nullable = false)
    private String studentId;

    @Column(name = "role", length = 20, nullable = false)
    private String role;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    public GroupMemberJpa() {
    }

    public String getMembershipId() { return membershipId; }
    public void setMembershipId(String membershipId) { this.membershipId = membershipId; }
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}
