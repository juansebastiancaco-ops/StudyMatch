package co.ufps.studymatch.infrastructure.persistence.student;

import co.ufps.studymatch.group.StudyGroupId;
import co.ufps.studymatch.student.AcademicProfile;
import co.ufps.studymatch.student.AcademicProfileId;
import co.ufps.studymatch.student.AvailabilitySlot;
import co.ufps.studymatch.student.AvailabilityStatus;
import co.ufps.studymatch.student.IStudentRepository;
import co.ufps.studymatch.student.Student;
import co.ufps.studymatch.student.StudentId;
import co.ufps.studymatch.student.StudentStatus;
import co.ufps.studymatch.student.TimeRange;
import co.ufps.studymatch.subject.SubjectId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.time.DayOfWeek;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class StudentRepositoryJpa implements IStudentRepository {
    @PersistenceContext
    EntityManager entityManager;

    @Override
    @Transactional
    public Student guardar(Student student) {
        entityManager.merge(toEntity(student));
        AcademicProfile profile = student.getAcademicProfile();
        entityManager.merge(toProfileEntity(profile, student.getStudentId().value()));

        entityManager.createQuery("DELETE FROM StudentSubjectJpa s WHERE s.studentId = :studentId")
                .setParameter("studentId", student.getStudentId().value())
                .executeUpdate();
        for (SubjectId subjectId : profile.getSubjects()) {
            StudentSubjectJpa relation = new StudentSubjectJpa();
            relation.setStudentSubjectId(UUID.randomUUID().toString());
            relation.setStudentId(student.getStudentId().value());
            relation.setSubjectId(subjectId.value());
            entityManager.persist(relation);
        }

        entityManager.createQuery("DELETE FROM AvailabilitySlotJpa a WHERE a.profileId = :profileId")
                .setParameter("profileId", profile.getProfileId().value())
                .executeUpdate();
        for (AvailabilitySlot slot : profile.getAvailabilitySlots()) {
            entityManager.persist(toSlotEntity(slot, profile.getProfileId().value()));
        }
        entityManager.flush();
        entityManager.clear();
        return student;
    }

    @Override
    public Optional<Student> buscarPorEmail(String email) {
        List<StudentJpa> result = entityManager.createQuery(
                        "SELECT s FROM StudentJpa s WHERE LOWER(s.email) = LOWER(:email)", StudentJpa.class)
                .setParameter("email", email)
                .getResultList();
        return result.stream().findFirst().map(this::toDomain);
    }

    @Override
    public boolean existeEmail(String email) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(s) FROM StudentJpa s WHERE LOWER(s.email) = LOWER(:email)", Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public Optional<Student> buscarId(StudentId studentId) {
        StudentJpa entity = entityManager.find(StudentJpa.class, studentId.value());
        return entity == null ? Optional.empty() : Optional.of(toDomain(entity));
    }

    @Override
    public List<Student> buscarTodos() {
        return entityManager.createQuery("SELECT s FROM StudentJpa s ORDER BY s.fullName ASC", StudentJpa.class)
                .getResultList().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Student> buscarPorIds(Collection<StudentId> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return List.of();
        }
        List<String> ids = studentIds.stream().map(StudentId::value).toList();
        return entityManager.createQuery(
                        "SELECT s FROM StudentJpa s WHERE s.studentId IN :ids ORDER BY s.fullName ASC", StudentJpa.class)
                .setParameter("ids", ids)
                .getResultList().stream().map(this::toDomain).toList();
    }

    private StudentJpa toEntity(Student student) {
        StudentJpa entity = new StudentJpa();
        entity.setStudentId(student.getStudentId().value());
        entity.setEmail(student.getEmail());
        entity.setPasswordHash(student.getPasswordHash());
        entity.setFullName(student.getFullName());
        entity.setCareer(student.getCareer());
        entity.setSemester(student.getSemester());
        entity.setStatus(student.getStatus().name());
        entity.setCreatedAt(student.getCreatedAt());
        return entity;
    }

    private AcademicProfileJpa toProfileEntity(AcademicProfile profile, String studentId) {
        AcademicProfileJpa entity = new AcademicProfileJpa();
        entity.setProfileId(profile.getProfileId().value());
        entity.setStudentId(studentId);
        entity.setBio(profile.getBio());
        entity.setAvailabilityState(profile.getAvailabilityStatus().name());
        entity.setCreatedAt(profile.getCreatedAt());
        entity.setUpdatedAt(profile.getUpdatedAt());
        return entity;
    }

    private AvailabilitySlotJpa toSlotEntity(AvailabilitySlot slot, String profileId) {
        AvailabilitySlotJpa entity = new AvailabilitySlotJpa();
        entity.setSlotId(slot.slotId());
        entity.setProfileId(profileId);
        entity.setDayOfWeek(slot.dayOfWeek().name());
        entity.setStartTime(slot.timeRange().start());
        entity.setEndTime(slot.timeRange().end());
        return entity;
    }

    private Student toDomain(StudentJpa entity) {
        AcademicProfileJpa profileEntity = entityManager.createQuery(
                        "SELECT p FROM AcademicProfileJpa p WHERE p.studentId = :studentId", AcademicProfileJpa.class)
                .setParameter("studentId", entity.getStudentId())
                .getResultList().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Perfil academico no encontrado para " + entity.getStudentId()));

        List<SubjectId> subjects = entityManager.createQuery(
                        "SELECT s.subjectId FROM StudentSubjectJpa s WHERE s.studentId = :studentId", String.class)
                .setParameter("studentId", entity.getStudentId())
                .getResultList().stream().map(SubjectId::of).toList();

        List<AvailabilitySlot> slots = entityManager.createQuery(
                        "SELECT a FROM AvailabilitySlotJpa a WHERE a.profileId = :profileId ORDER BY a.dayOfWeek, a.startTime",
                        AvailabilitySlotJpa.class)
                .setParameter("profileId", profileEntity.getProfileId())
                .getResultList().stream()
                .map(this::toSlotDomain)
                .toList();

        Set<StudyGroupId> activeGroups = new LinkedHashSet<>(entityManager.createQuery(
                        "SELECT gm.groupId FROM GroupMemberJpa gm WHERE gm.studentId = :studentId "
                                + "AND gm.groupId IN (SELECT g.groupId FROM StudyGroupJpa g WHERE g.status IN :statuses)",
                        String.class)
                .setParameter("studentId", entity.getStudentId())
                .setParameter("statuses", List.of("OPEN", "FULL", "IN_PROGRESS"))
                .getResultList().stream().map(StudyGroupId::of).toList());

        AcademicProfile profile = AcademicProfile.of(
                AcademicProfileId.of(profileEntity.getProfileId()),
                profileEntity.getBio(),
                subjects,
                AvailabilityStatus.valueOf(profileEntity.getAvailabilityState()),
                slots,
                profileEntity.getCreatedAt(),
                profileEntity.getUpdatedAt()
        );

        return Student.of(
                StudentId.of(entity.getStudentId()),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getFullName(),
                entity.getCareer(),
                entity.getSemester(),
                StudentStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                profile,
                activeGroups
        );
    }

    private AvailabilitySlot toSlotDomain(AvailabilitySlotJpa entity) {
        return AvailabilitySlot.of(
                entity.getSlotId(),
                DayOfWeek.valueOf(entity.getDayOfWeek()),
                new TimeRange(entity.getStartTime(), entity.getEndTime())
        );
    }
}
