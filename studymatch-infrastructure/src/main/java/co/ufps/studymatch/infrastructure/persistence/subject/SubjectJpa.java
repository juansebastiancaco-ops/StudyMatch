package co.ufps.studymatch.infrastructure.persistence.subject;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "subjects", indexes = {
        @Index(name = "idx_subjects_code", columnList = "code", unique = true),
        @Index(name = "idx_subjects_name", columnList = "name")
})
public class SubjectJpa {
    @Id
    @Column(name = "subject_id", length = 36, nullable = false)
    private String subjectId;

    @Column(name = "code", length = 30, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "area", length = 100, nullable = false)
    private String area;

    @Column(name = "credits", nullable = false)
    private int credits;

    @Column(name = "semester", nullable = false)
    private int semester;

    @Column(name = "active", nullable = false)
    private boolean active;

    public SubjectJpa() {
    }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }
    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
