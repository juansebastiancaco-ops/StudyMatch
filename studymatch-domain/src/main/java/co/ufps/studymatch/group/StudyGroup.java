package co.ufps.studymatch.group;

import co.ufps.studymatch.ConflictException;
import co.ufps.studymatch.ValidationException;
import co.ufps.studymatch.student.AvailabilitySlot;
import co.ufps.studymatch.student.StudentId;
import co.ufps.studymatch.subject.SubjectId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Agregado raiz del contexto de grupos de estudio.
 */
public class StudyGroup {
    private static final int MIN_MEMBERS = 2;
    private static final int MAX_MEMBERS_LIMIT = 50;

    private final StudyGroupId groupId;
    private final SubjectId subjectId;
    private final String name;
    private final String description;
    private final StudentId creatorId;
    private final int maxMembers;
    private final List<AvailabilitySlot> schedule;
    private final GroupModality modality;
    private GroupStatus status;
    private final LocalDateTime createdAt;
    private final List<GroupMembership> members;
    private final List<JoinRequest> requests;

    private StudyGroup(StudyGroupId groupId,
                       SubjectId subjectId,
                       String name,
                       String description,
                       StudentId creatorId,
                       int maxMembers,
                       List<AvailabilitySlot> schedule,
                       GroupModality modality,
                       GroupStatus status,
                       LocalDateTime createdAt,
                       Collection<GroupMembership> members,
                       Collection<JoinRequest> requests) {
        this.groupId = Objects.requireNonNull(groupId, "El groupId es obligatorio");
        this.subjectId = Objects.requireNonNull(subjectId, "El subjectId es obligatorio");
        this.name = validateName(name);
        this.description = validateDescription(description);
        this.creatorId = Objects.requireNonNull(creatorId, "El creatorId es obligatorio");
        if (maxMembers < MIN_MEMBERS || maxMembers > MAX_MEMBERS_LIMIT) {
            throw new ValidationException("La capacidad del grupo debe estar entre " + MIN_MEMBERS
                    + " y " + MAX_MEMBERS_LIMIT + " estudiantes");
        }
        this.maxMembers = maxMembers;
        this.schedule = validateSchedule(schedule);
        this.modality = Objects.requireNonNull(modality, "La modalidad es obligatoria");
        this.status = Objects.requireNonNull(status, "El estado del grupo es obligatorio");
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.members = new ArrayList<>(members != null ? members : List.of());
        this.requests = new ArrayList<>(requests != null ? requests : List.of());
    }

    public static StudyGroup create(SubjectId subjectId,
                                    String name,
                                    String description,
                                    StudentId creatorId,
                                    int maxMembers,
                                    List<AvailabilitySlot> schedule,
                                    GroupModality modality) {
        StudyGroupId groupId = StudyGroupId.generate();
        GroupMembership leader = GroupMembership.create(groupId, creatorId, GroupMemberRole.LEADER);
        return new StudyGroup(groupId, subjectId, name, description, creatorId, maxMembers, schedule,
                modality, GroupStatus.DRAFT, LocalDateTime.now(), List.of(leader), List.of());
    }

    public static StudyGroup of(StudyGroupId groupId,
                                SubjectId subjectId,
                                String name,
                                String description,
                                StudentId creatorId,
                                int maxMembers,
                                List<AvailabilitySlot> schedule,
                                GroupModality modality,
                                GroupStatus status,
                                LocalDateTime createdAt,
                                Collection<GroupMembership> members,
                                Collection<JoinRequest> requests) {
        return new StudyGroup(groupId, subjectId, name, description, creatorId, maxMembers, schedule,
                modality, status, createdAt, members, requests);
    }

    public void changeStatus(StudentId actorId, GroupStatus target) {
        requireLeader(actorId);
        if (target == GroupStatus.FULL && members.size() < maxMembers) {
            throw new ConflictException("El grupo solo puede marcarse FULL cuando alcanza su capacidad maxima");
        }
        if (target == GroupStatus.OPEN && status == GroupStatus.FULL && members.size() >= maxMembers) {
            throw new ConflictException("El grupo sigue lleno; no puede volver a OPEN");
        }
        this.status = status.transitionTo(target);
    }

    public JoinRequest sendJoinRequest(StudentId studentId, String message, boolean scheduleCompatible) {
        Objects.requireNonNull(studentId, "El studentId es obligatorio");
        if (!status.acceptsRequests()) {
            throw new ConflictException("Solo los grupos en estado OPEN aceptan solicitudes");
        }
        if (isMember(studentId)) {
            throw new AlreadyMemberException();
        }
        if (!scheduleCompatible) {
            throw new ScheduleConflictException();
        }
        if (hasPendingRequestFrom(studentId)) {
            throw new DuplicateJoinRequestException();
        }
        JoinRequest request = JoinRequest.create(groupId, studentId, message);
        requests.add(request);
        return request;
    }

    public JoinRequest acceptRequest(JoinRequestId requestId, StudentId actorId) {
        requireLeader(actorId);
        if (members.size() >= maxMembers) {
            if (status == GroupStatus.OPEN) {
                status = GroupStatus.FULL;
            }
            throw new GroupFullException(groupId.value());
        }
        if (!status.acceptsRequests()) {
            throw new ConflictException("Solo se pueden aceptar solicitudes de grupos en estado OPEN");
        }
        JoinRequest request = findRequest(requestId);
        if (!request.isPending()) {
            throw new InvalidRequestStateException("La solicitud no esta pendiente");
        }
        request.accept();
        members.add(GroupMembership.create(groupId, request.getStudentId(), GroupMemberRole.MEMBER));
        if (members.size() >= maxMembers) {
            status = GroupStatus.FULL;
        }
        return request;
    }

    public JoinRequest rejectRequest(JoinRequestId requestId, StudentId actorId) {
        requireLeader(actorId);
        JoinRequest request = findRequest(requestId);
        request.reject();
        return request;
    }

    public JoinRequest cancelRequest(JoinRequestId requestId, StudentId studentId) {
        JoinRequest request = findRequest(requestId);
        if (!request.getStudentId().equals(studentId)) {
            throw new ConflictException("Solo el solicitante puede cancelar su solicitud");
        }
        request.cancel();
        return request;
    }

    public GroupMembership leaveGroup(StudentId studentId) {
        GroupMembership membership = members.stream()
                .filter(member -> member.getStudentId().equals(studentId))
                .findFirst()
                .orElseThrow(() -> new NotGroupMemberException(studentId.value()));
        members.remove(membership);
        if (membership.isLeader()) {
            members.stream().findFirst().ifPresent(GroupMembership::promoteToLeader);
        }
        if (members.isEmpty()) {
            status = GroupStatus.ARCHIVED;
        } else if (status == GroupStatus.FULL && members.size() < maxMembers) {
            status = GroupStatus.OPEN;
        }
        return membership;
    }

    /**
     * Restauracion usada por Command.undo() despues de LeaveGroupAction.
     */
    public void restoreMembership(GroupMembership membership) {
        if (isMember(membership.getStudentId())) {
            return;
        }
        if (membership.isLeader()) {
            members.forEach(GroupMembership::demoteToMember);
        }
        members.add(membership);
        if (status == GroupStatus.ARCHIVED) {
            status = GroupStatus.OPEN;
        }
        if (members.size() >= maxMembers) {
            status = GroupStatus.FULL;
        }
    }

    public void revertAcceptedRequest(JoinRequestId requestId) {
        JoinRequest request = findRequest(requestId);
        if (request.getStatus() != RequestStatus.ACCEPTED) {
            throw new InvalidRequestStateException("La solicitud no esta aceptada");
        }
        members.removeIf(member -> member.getStudentId().equals(request.getStudentId())
                && !member.isLeader());
        request.reopen();
        if (status == GroupStatus.FULL && members.size() < maxMembers) {
            status = GroupStatus.OPEN;
        }
    }

    public void revertRejectedRequest(JoinRequestId requestId) {
        JoinRequest request = findRequest(requestId);
        if (request.getStatus() != RequestStatus.REJECTED) {
            throw new InvalidRequestStateException("La solicitud no esta rechazada");
        }
        request.reopen();
    }

    public void revertCancelledRequest(JoinRequestId requestId) {
        JoinRequest request = findRequest(requestId);
        if (request.getStatus() != RequestStatus.CANCELLED) {
            throw new InvalidRequestStateException("La solicitud no esta cancelada");
        }
        request.reopen();
    }

    public boolean isLeader(StudentId studentId) {
        return members.stream()
                .anyMatch(member -> member.isLeader() && member.getStudentId().equals(studentId));
    }

    public boolean isMember(StudentId studentId) {
        return members.stream().anyMatch(member -> member.getStudentId().equals(studentId));
    }

    public boolean hasPendingRequestFrom(StudentId studentId) {
        return requests.stream()
                .anyMatch(request -> request.getStudentId().equals(studentId) && request.isPending());
    }

    public Optional<GroupMemberRole> roleOf(StudentId studentId) {
        return members.stream()
                .filter(member -> member.getStudentId().equals(studentId))
                .map(GroupMembership::getRole)
                .findFirst();
    }

    public Optional<StudentId> getLeaderId() {
        return members.stream()
                .filter(GroupMembership::isLeader)
                .map(GroupMembership::getStudentId)
                .findFirst();
    }

    private void requireLeader(StudentId actorId) {
        if (!isLeader(actorId)) {
            throw new NotGroupLeaderException();
        }
    }

    private JoinRequest findRequest(JoinRequestId requestId) {
        return requests.stream()
                .filter(request -> request.getRequestId().equals(requestId))
                .findFirst()
                .orElseThrow(() -> new JoinRequestNotFoundException(requestId.value()));
    }

    private static String validateName(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("El nombre del grupo es obligatorio");
        }
        String normalized = value.strip();
        if (normalized.length() < 3 || normalized.length() > 100) {
            throw new ValidationException("El nombre del grupo debe tener entre 3 y 100 caracteres");
        }
        return normalized;
    }

    private static String validateDescription(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.strip();
        if (normalized.length() > 1000) {
            throw new ValidationException("La descripcion del grupo no puede superar 1000 caracteres");
        }
        return normalized;
    }

    private static List<AvailabilitySlot> validateSchedule(List<AvailabilitySlot> schedule) {
        if (schedule == null || schedule.isEmpty()) {
            throw new ValidationException("El grupo debe tener al menos un horario");
        }
        for (int i = 0; i < schedule.size(); i++) {
            if (schedule.get(i) == null) {
                throw new ValidationException("El horario no puede contener valores nulos");
            }
            for (int j = i + 1; j < schedule.size(); j++) {
                if (schedule.get(i).overlaps(schedule.get(j))) {
                    throw new ValidationException("Los horarios del grupo no pueden solaparse");
                }
            }
        }
        return List.copyOf(schedule);
    }

    public StudyGroupId getGroupId() {
        return groupId;
    }

    public SubjectId getSubjectId() {
        return subjectId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public StudentId getCreatorId() {
        return creatorId;
    }

    public int getMaxMembers() {
        return maxMembers;
    }

    public List<AvailabilitySlot> getSchedule() {
        return schedule;
    }

    public GroupModality getModality() {
        return modality;
    }

    public GroupStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<GroupMembership> getMembers() {
        return List.copyOf(members);
    }

    public List<JoinRequest> getRequests() {
        return List.copyOf(requests);
    }

    public List<JoinRequest> getPendingRequests() {
        return requests.stream().filter(JoinRequest::isPending).toList();
    }

    public List<StudentId> getMemberIds() {
        return members.stream().map(GroupMembership::getStudentId).toList();
    }

    public int getMemberCount() {
        return members.size();
    }

    public int getAvailableSpots() {
        return Math.max(0, maxMembers - members.size());
    }
}
