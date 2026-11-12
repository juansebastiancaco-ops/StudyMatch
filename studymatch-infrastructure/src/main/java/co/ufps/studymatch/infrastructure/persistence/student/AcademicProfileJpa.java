package co.ufps.studymatch.infrastructure.persistence.student;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "academic_profiles", indexes = {
        @Index(name = "idx_profiles_student", columnList = "student_id", unique = true)
})
public class AcademicProfileJpa {
    @Id
    @Column(name = "profile_id", length = 36, nullable = false)
    private String profileId;

    @Column(name = "student_id", length = 36, nullable = false, unique = true)
    private String studentId;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "availability_state", length = 20, nullable = false)
    private String availabilityState;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public AcademicProfileJpa() {
    }

    public String getProfileId() { return profileId; }
    public void setProfileId(String profileId) { this.profileId = profileId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getAvailabilityState() { return availabilityState; }
    public void setAvailabilityState(String availabilityState) { this.availabilityState = availabilityState; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
