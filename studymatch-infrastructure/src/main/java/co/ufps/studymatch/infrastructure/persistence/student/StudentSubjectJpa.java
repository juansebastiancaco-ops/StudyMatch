package co.ufps.studymatch.infrastructure.persistence.student;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "student_subjects",
        uniqueConstraints = @UniqueConstraint(name = "uk_student_subject", columnNames = {"student_id", "subject_id"}),
        indexes = @Index(name = "idx_student_subjects_student", columnList = "student_id"))
public class StudentSubjectJpa {
    @Id
    @Column(name = "student_subject_id", length = 36, nullable = false)
    private String studentSubjectId;

    @Column(name = "student_id", length = 36, nullable = false)
    private String studentId;

    @Column(name = "subject_id", length = 36, nullable = false)
    private String subjectId;

    public StudentSubjectJpa() {
    }

    public String getStudentSubjectId() { return studentSubjectId; }
    public void setStudentSubjectId(String studentSubjectId) { this.studentSubjectId = studentSubjectId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }
}
