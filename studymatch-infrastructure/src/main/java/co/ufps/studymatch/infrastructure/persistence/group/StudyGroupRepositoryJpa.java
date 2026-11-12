package co.ufps.studymatch.infrastructure.persistence.group;

import co.ufps.studymatch.group.GroupMemberRole;
import co.ufps.studymatch.group.GroupMembership;
import co.ufps.studymatch.group.GroupModality;
import co.ufps.studymatch.group.GroupStatus;
import co.ufps.studymatch.group.IStudyGroupRepository;
import co.ufps.studymatch.group.JoinRequest;
import co.ufps.studymatch.group.JoinRequestId;
import co.ufps.studymatch.group.RequestStatus;
import co.ufps.studymatch.group.StudyGroup;
import co.ufps.studymatch.group.StudyGroupId;
import co.ufps.studymatch.student.AvailabilitySlot;
import co.ufps.studymatch.student.StudentId;
import co.ufps.studymatch.student.TimeRange;
import co.ufps.studymatch.subject.SubjectId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class StudyGroupRepositoryJpa implements IStudyGroupRepository {
    @PersistenceContext
    EntityManager entityManager;

    @Override
    @Transactional
    public StudyGroup guardar(StudyGroup group) {
        entityManager.merge(toEntity(group));

        entityManager.createQuery("DELETE FROM GroupScheduleJpa s WHERE s.groupId = :groupId")
                .setParameter("groupId", group.getGroupId().value()).executeUpdate();
        for (AvailabilitySlot slot : group.getSchedule()) {
            GroupScheduleJpa entity = new GroupScheduleJpa();
            entity.setSlotId(slot.slotId());
            entity.setGroupId(group.getGroupId().value());
            entity.setDayOfWeek(slot.dayOfWeek().name());
            entity.setStartTime(slot.timeRange().start());
            entity.setEndTime(slot.timeRange().end());
            entityManager.persist(entity);
        }

        entityManager.createQuery("DELETE FROM GroupMemberJpa m WHERE m.groupId = :groupId")
                .setParameter("groupId", group.getGroupId().value()).executeUpdate();
        for (GroupMembership membership : group.getMembers()) {
            GroupMemberJpa entity = new GroupMemberJpa();
            entity.setMembershipId(membership.getMembershipId());
            entity.setGroupId(group.getGroupId().value());
            entity.setStudentId(membership.getStudentId().value());
            entity.setRole(membership.getRole().name());
            entity.setJoinedAt(membership.getJoinedAt());
            entityManager.persist(entity);
        }

        entityManager.createQuery("DELETE FROM JoinRequestJpa r WHERE r.groupId = :groupId")
                .setParameter("groupId", group.getGroupId().value()).executeUpdate();
        for (JoinRequest request : group.getRequests()) {
            JoinRequestJpa entity = new JoinRequestJpa();
            entity.setRequestId(request.getRequestId().value());
            entity.setGroupId(group.getGroupId().value());
            entity.setStudentId(request.getStudentId().value());
            entity.setMessage(request.getMessage());
            entity.setStatus(request.getStatus().name());
            entity.setCreatedAt(request.getCreatedAt());
            entity.setResolvedAt(request.getResolvedAt());
            entityManager.persist(entity);
        }
        entityManager.flush();
        entityManager.clear();
        return group;
    }

    @Override
    public Optional<StudyGroup> buscarId(StudyGroupId groupId) {
        StudyGroupJpa entity = entityManager.find(StudyGroupJpa.class, groupId.value());
        return entity == null ? Optional.empty() : Optional.of(toDomain(entity));
    }

    @Override
    public List<StudyGroup> buscar(SubjectId subjectId, GroupStatus status, String texto,
                                   GroupModality modality, int offset, int limit) {
        QueryParts parts = buildQuery(subjectId, status, texto, modality, false);
        TypedQuery<StudyGroupJpa> query = entityManager.createQuery(
                parts.query + " ORDER BY g.createdAt DESC", StudyGroupJpa.class);
        parts.parameters.applyTo(query);
        return query.setFirstResult(Math.max(0, offset)).setMaxResults(Math.max(1, limit))
                .getResultList().stream().map(this::toDomain).toList();
    }

    @Override
    public long contar(SubjectId subjectId, GroupStatus status, String texto, GroupModality modality) {
        QueryParts parts = buildQuery(subjectId, status, texto, modality, true);
        TypedQuery<Long> query = entityManager.createQuery(parts.query, Long.class);
        parts.parameters.applyTo(query);
        return query.getSingleResult();
    }

    @Override
    public List<StudyGroup> buscarTodos() {
        return entityManager.createQuery("SELECT g FROM StudyGroupJpa g ORDER BY g.createdAt DESC", StudyGroupJpa.class)
                .getResultList().stream().map(this::toDomain).toList();
    }

    @Override
    public List<StudyGroup> buscarPorIds(Collection<StudyGroupId> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return List.of();
        }
        List<String> ids = groupIds.stream().map(StudyGroupId::value).toList();
        return entityManager.createQuery(
                        "SELECT g FROM StudyGroupJpa g WHERE g.groupId IN :ids ORDER BY g.createdAt DESC",
                        StudyGroupJpa.class)
                .setParameter("ids", ids)
                .getResultList().stream().map(this::toDomain).toList();
    }

    @Override
    public List<StudyGroup> buscarPorEstudiante(StudentId studentId) {
        return entityManager.createQuery(
                        "SELECT g FROM StudyGroupJpa g WHERE g.groupId IN "
                                + "(SELECT m.groupId FROM GroupMemberJpa m WHERE m.studentId = :studentId) "
                                + "ORDER BY g.createdAt DESC", StudyGroupJpa.class)
                .setParameter("studentId", studentId.value())
                .getResultList().stream().map(this::toDomain).toList();
    }

    @Override
    public long contarGruposActivos(StudentId studentId) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(m) FROM GroupMemberJpa m, StudyGroupJpa g "
                                + "WHERE m.groupId = g.groupId AND m.studentId = :studentId AND g.status IN :statuses",
                        Long.class)
                .setParameter("studentId", studentId.value())
                .setParameter("statuses", List.of("OPEN", "FULL", "IN_PROGRESS"))
                .getSingleResult();
        return count;
    }

    @Override
    public long contarSolicitudesPendientes(StudentId studentId) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(r) FROM JoinRequestJpa r WHERE r.studentId = :studentId AND r.status = 'PENDING'",
                        Long.class)
                .setParameter("studentId", studentId.value())
                .getSingleResult();
        return count;
    }

    @Override
    public long contarSolicitudesRecibidasPendientes(StudentId studentId) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(r) FROM JoinRequestJpa r WHERE r.status = 'PENDING' AND r.groupId IN "
                                + "(SELECT m.groupId FROM GroupMemberJpa m WHERE m.studentId = :studentId AND m.role = 'LEADER')",
                        Long.class)
                .setParameter("studentId", studentId.value())
                .getSingleResult();
        return count;
    }

    @Override
    @Transactional
    public int borrar(StudyGroupId groupId) {
        entityManager.createQuery("DELETE FROM GroupScheduleJpa s WHERE s.groupId = :groupId")
                .setParameter("groupId", groupId.value()).executeUpdate();
        entityManager.createQuery("DELETE FROM GroupMemberJpa m WHERE m.groupId = :groupId")
                .setParameter("groupId", groupId.value()).executeUpdate();
        entityManager.createQuery("DELETE FROM JoinRequestJpa r WHERE r.groupId = :groupId")
                .setParameter("groupId", groupId.value()).executeUpdate();
        return entityManager.createQuery("DELETE FROM StudyGroupJpa g WHERE g.groupId = :groupId")
                .setParameter("groupId", groupId.value()).executeUpdate();
    }

    private QueryParts buildQuery(SubjectId subjectId, GroupStatus status, String texto,
                                  GroupModality modality, boolean count) {
        StringBuilder query = new StringBuilder(count ? "SELECT COUNT(g) " : "SELECT g ")
                .append("FROM StudyGroupJpa g WHERE 1 = 1");
        Parameters parameters = new Parameters();
        if (subjectId != null) {
            query.append(" AND g.subjectId = :subjectId");
            parameters.subjectId = subjectId.value();
        }
        if (status != null) {
            query.append(" AND g.status = :status");
            parameters.status = status.name();
        }
        if (texto != null && !texto.isBlank()) {
            query.append(" AND (LOWER(g.name) LIKE :texto OR LOWER(g.description) LIKE :texto)");
            parameters.texto = "%" + texto.strip().toLowerCase() + "%";
        }
        if (modality != null) {
            query.append(" AND g.modality = :modality");
            parameters.modality = modality.name();
        }
        return new QueryParts(query.toString(), parameters);
    }

    private StudyGroupJpa toEntity(StudyGroup group) {
        StudyGroupJpa entity = new StudyGroupJpa();
        entity.setGroupId(group.getGroupId().value());
        entity.setSubjectId(group.getSubjectId().value());
        entity.setName(group.getName());
        entity.setDescription(group.getDescription());
        entity.setCreatorId(group.getCreatorId().value());
        entity.setMaxMembers(group.getMaxMembers());
        entity.setModality(group.getModality().name());
        entity.setStatus(group.getStatus().name());
        entity.setCreatedAt(group.getCreatedAt());
        return entity;
    }

    private StudyGroup toDomain(StudyGroupJpa entity) {
        List<AvailabilitySlot> schedule = entityManager.createQuery(
                        "SELECT s FROM GroupScheduleJpa s WHERE s.groupId = :groupId ORDER BY s.dayOfWeek, s.startTime",
                        GroupScheduleJpa.class)
                .setParameter("groupId", entity.getGroupId())
                .getResultList().stream()
                .map(slot -> AvailabilitySlot.of(slot.getSlotId(), DayOfWeek.valueOf(slot.getDayOfWeek()),
                        new TimeRange(slot.getStartTime(), slot.getEndTime())))
                .toList();

        List<GroupMembership> members = entityManager.createQuery(
                        "SELECT m FROM GroupMemberJpa m WHERE m.groupId = :groupId ORDER BY m.joinedAt ASC",
                        GroupMemberJpa.class)
                .setParameter("groupId", entity.getGroupId())
                .getResultList().stream()
                .map(member -> GroupMembership.of(member.getMembershipId(), StudyGroupId.of(entity.getGroupId()),
                        StudentId.of(member.getStudentId()), GroupMemberRole.valueOf(member.getRole()),
                        member.getJoinedAt()))
                .toList();

        List<JoinRequest> requests = entityManager.createQuery(
                        "SELECT r FROM JoinRequestJpa r WHERE r.groupId = :groupId ORDER BY r.createdAt ASC",
                        JoinRequestJpa.class)
                .setParameter("groupId", entity.getGroupId())
                .getResultList().stream()
                .map(request -> JoinRequest.of(JoinRequestId.of(request.getRequestId()),
                        StudyGroupId.of(entity.getGroupId()), StudentId.of(request.getStudentId()),
                        request.getMessage(), RequestStatus.valueOf(request.getStatus()),
                        request.getCreatedAt(), request.getResolvedAt()))
                .toList();

        return StudyGroup.of(
                StudyGroupId.of(entity.getGroupId()),
                SubjectId.of(entity.getSubjectId()),
                entity.getName(),
                entity.getDescription(),
                StudentId.of(entity.getCreatorId()),
                entity.getMaxMembers(),
                schedule,
                GroupModality.valueOf(entity.getModality()),
                GroupStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                members,
                requests
        );
    }

    private record QueryParts(String query, Parameters parameters) {
    }

    private static final class Parameters {
        String subjectId;
        String status;
        String texto;
        String modality;

        void applyTo(TypedQuery<?> query) {
            if (subjectId != null) query.setParameter("subjectId", subjectId);
            if (status != null) query.setParameter("status", status);
            if (texto != null) query.setParameter("texto", texto);
            if (modality != null) query.setParameter("modality", modality);
        }
    }
}
