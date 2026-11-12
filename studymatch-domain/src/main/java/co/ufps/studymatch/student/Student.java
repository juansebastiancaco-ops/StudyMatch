package co.ufps.studymatch.student;

import co.ufps.studymatch.ConflictException;
import co.ufps.studymatch.ValidationException;
import co.ufps.studymatch.group.StudyGroupId;
import co.ufps.studymatch.subject.SubjectId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Agregado raiz del contexto de identidad y perfil academico.
 */
public class Student {
    public static final int MAX_ACTIVE_GROUPS = 5;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final StudentId studentId;
    private final String email;
    private final String passwordHash;
    private String fullName;
    private String career;
    private int semester;
    private StudentStatus status;
    private final LocalDateTime createdAt;
    private final AcademicProfile academicProfile;
    private final Set<StudyGroupId> activeGroupIds;

    private Student(StudentId studentId,
                    String email,
                    String passwordHash,
                    String fullName,
                    String career,
                    int semester,
                    StudentStatus status,
                    LocalDateTime createdAt,
                    AcademicProfile academicProfile,
                    Collection<StudyGroupId> activeGroupIds) {
        this.studentId = Objects.requireNonNull(studentId, "El studentId es obligatorio");
        this.email = normalizeEmail(email);
        this.passwordHash = requireText(passwordHash, "El passwordHash es obligatorio");
        this.fullName = requireText(fullName, "El nombre completo es obligatorio");
        this.career = requireText(career, "La carrera es obligatoria");
        this.semester = validateSemester(semester);
        this.status = Objects.requireNonNull(status, "El estado del estudiante es obligatorio");
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.academicProfile = Objects.requireNonNull(academicProfile, "El perfil academico es obligatorio");
        this.activeGroupIds = new LinkedHashSet<>(activeGroupIds != null ? activeGroupIds : List.of());
        if (this.activeGroupIds.size() > MAX_ACTIVE_GROUPS) {
            throw new ConflictException("Un estudiante no puede tener mas de " + MAX_ACTIVE_GROUPS
                    + " grupos activos");
        }
    }

    public static Student register(String email,
                                   String passwordHash,
                                   String fullName,
                                   String career,
                                   int semester,
                                   String bio,
                                   List<SubjectId> subjects,
                                   AvailabilityStatus availabilityStatus,
                                   List<AvailabilitySlot> availabilitySlots) {
        AcademicProfile profile = AcademicProfile.create(bio, subjects, availabilityStatus, availabilitySlots);
        return new Student(
                StudentId.generate(),
                email,
                passwordHash,
                fullName,
                career,
                semester,
                StudentStatus.ACTIVE,
                LocalDateTime.now(),
                profile,
                List.of()
        );
    }

    public static Student of(StudentId studentId,
                             String email,
                             String passwordHash,
                             String fullName,
                             String career,
                             int semester,
                             StudentStatus status,
                             LocalDateTime createdAt,
                             AcademicProfile academicProfile,
                             Collection<StudyGroupId> activeGroupIds) {
        return new Student(studentId, email, passwordHash, fullName, career, semester, status,
                createdAt, academicProfile, activeGroupIds);
    }

    public void updateProfile(String career, int semester, String bio, List<SubjectId> subjects) {
        this.career = requireText(career, "La carrera es obligatoria");
        this.semester = validateSemester(semester);
        this.academicProfile.update(bio, subjects);
    }

    public AvailabilityStatus changeAvailability(AvailabilityStatus target, List<AvailabilitySlot> slots) {
        return academicProfile.changeAvailability(target, slots);
    }

    public void restoreAvailability(AvailabilityStatus target, List<AvailabilitySlot> slots) {
        academicProfile.restoreAvailability(target, slots);
    }

    public void joinGroup(StudyGroupId groupId) {
        Objects.requireNonNull(groupId, "El groupId es obligatorio");
        if (activeGroupIds.contains(groupId)) {
            return;
        }
        if (activeGroupIds.size() >= MAX_ACTIVE_GROUPS) {
            throw new MaxActiveGroupsExceededException(MAX_ACTIVE_GROUPS);
        }
        activeGroupIds.add(groupId);
    }

    public void leaveGroup(StudyGroupId groupId) {
        activeGroupIds.remove(groupId);
    }

    public boolean isMemberOf(StudyGroupId groupId) {
        return activeGroupIds.contains(groupId);
    }

    public boolean hasSubject(SubjectId subjectId) {
        return academicProfile.hasSubject(subjectId);
    }

    public boolean hasScheduleOverlapWith(List<AvailabilitySlot> groupSchedule) {
        if (groupSchedule == null || groupSchedule.isEmpty()) {
            return true;
        }
        if (academicProfile.getAvailabilitySlots().isEmpty()) {
            // Sin disponibilidad declarada no se puede afirmar que exista choque; se permite y las
            // recomendaciones penalizan el puntaje de horario.
            return true;
        }
        return groupSchedule.stream().anyMatch(groupSlot ->
                academicProfile.getAvailabilitySlots().stream().anyMatch(studentSlot -> studentSlot.overlaps(groupSlot)));
    }

    public boolean isActive() {
        return status.canAccessPlatform();
    }

    public void suspend() {
        this.status = StudentStatus.SUSPENDED;
    }

    public void activate() {
        this.status = StudentStatus.ACTIVE;
    }

    private static String normalizeEmail(String value) {
        String email = requireText(value, "El email es obligatorio").toLowerCase();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("El email no tiene un formato valido");
        }
        return email;
    }

    private static int validateSemester(int semester) {
        if (semester < 1 || semester > 12) {
            throw new ValidationException("El semestre debe estar entre 1 y 12");
        }
        return semester;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
        return value.strip();
    }

    public StudentId getStudentId() {
        return studentId;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public String getCareer() {
        return career;
    }

    public int getSemester() {
        return semester;
    }

    public StudentStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public AcademicProfile getAcademicProfile() {
        return academicProfile;
    }

    public Set<StudyGroupId> getActiveGroupIds() {
        return Set.copyOf(activeGroupIds);
    }

    public int getActiveGroupCount() {
        return activeGroupIds.size();
    }

    public List<SubjectId> getSubjectIds() {
        return academicProfile.getSubjects();
    }

    public List<AvailabilitySlot> getAvailabilitySlots() {
        return academicProfile.getAvailabilitySlots();
    }

    public AvailabilityStatus getAvailabilityStatus() {
        return academicProfile.getAvailabilityStatus();
    }

    public List<SubjectId> sharedSubjectsWith(Student other) {
        return academicProfile.sharedSubjectsWith(other.academicProfile);
    }

    public static List<StudyGroupId> copyGroupIds(Collection<StudyGroupId> ids) {
        return new ArrayList<>(ids);
    }
}
