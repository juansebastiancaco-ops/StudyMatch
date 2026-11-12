package co.ufps.studymatch.application.group;

import co.ufps.studymatch.application.common.SubjectNotFoundException;
import co.ufps.studymatch.application.student.AvailabilitySlotDTO;
import co.ufps.studymatch.application.subject.SubjectDTO;
import co.ufps.studymatch.group.GroupMembership;
import co.ufps.studymatch.group.IStudyGroupRepository;
import co.ufps.studymatch.group.JoinRequest;
import co.ufps.studymatch.group.StudyGroup;
import co.ufps.studymatch.student.IStudentRepository;
import co.ufps.studymatch.student.Student;
import co.ufps.studymatch.student.StudentId;
import co.ufps.studymatch.subject.ISubjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class GroupDtoAssembler {
    private final ISubjectRepository subjectRepository;
    private final IStudentRepository studentRepository;

    @Inject
    public GroupDtoAssembler(ISubjectRepository subjectRepository, IStudentRepository studentRepository) {
        this.subjectRepository = subjectRepository;
        this.studentRepository = studentRepository;
    }

    public StudyGroupSummaryDTO toSummary(StudyGroup group, Double affinityScore, List<String> reasons) {
        SubjectDTO subject = subjectRepository.buscarId(group.getSubjectId())
                .map(SubjectDTO::from)
                .orElseThrow(() -> new SubjectNotFoundException(group.getSubjectId().value()));
        Student creator = studentRepository.buscarId(group.getCreatorId()).orElse(null);
        return new StudyGroupSummaryDTO(
                group.getGroupId().value(),
                subject,
                group.getName(),
                group.getDescription(),
                group.getCreatorId().value(),
                creator != null ? creator.getFullName() : "Estudiante",
                group.getMaxMembers(),
                group.getMemberCount(),
                group.getAvailableSpots(),
                group.getModality().name(),
                group.getStatus().name(),
                group.getCreatedAt(),
                group.getSchedule().stream().map(AvailabilitySlotDTO::from).toList(),
                affinityScore,
                reasons == null ? List.of() : List.copyOf(reasons)
        );
    }

    public StudyGroupDetailDTO toDetail(StudyGroup group, Student viewer) {
        StudyGroupSummaryDTO summary = toSummary(group, null, List.of());

        Set<StudentId> studentIds = new LinkedHashSet<>(group.getMemberIds());
        group.getRequests().forEach(request -> studentIds.add(request.getStudentId()));
        Map<StudentId, Student> students = studentRepository.buscarPorIds(studentIds).stream()
                .collect(Collectors.toMap(Student::getStudentId, Function.identity()));

        List<GroupMemberDTO> members = group.getMembers().stream()
                .map(membership -> GroupMemberDTO.from(membership, students.get(membership.getStudentId())))
                .toList();

        boolean viewerIsLeader = viewer != null && group.isLeader(viewer.getStudentId());
        List<JoinRequestDTO> requests = group.getRequests().stream()
                .filter(request -> viewerIsLeader
                        || (viewer != null && request.getStudentId().equals(viewer.getStudentId())))
                .map(request -> JoinRequestDTO.from(request, students.get(request.getStudentId())))
                .toList();

        String viewerRole = viewer == null ? "GUEST"
                : group.roleOf(viewer.getStudentId()).map(Enum::name).orElse("GUEST");

        return StudyGroupDetailDTO.from(summary, members, requests, viewerRole);
    }

    public JoinRequestDTO toRequestDto(JoinRequest request) {
        Student student = studentRepository.buscarId(request.getStudentId()).orElse(null);
        return JoinRequestDTO.from(request, student);
    }

    public List<JoinRequestDTO> toRequestDtos(List<JoinRequest> requests) {
        Set<StudentId> ids = requests.stream().map(JoinRequest::getStudentId).collect(Collectors.toCollection(LinkedHashSet::new));
        Map<StudentId, Student> students = studentRepository.buscarPorIds(ids).stream()
                .collect(Collectors.toMap(Student::getStudentId, Function.identity()));
        return requests.stream()
                .map(request -> JoinRequestDTO.from(request, students.get(request.getStudentId())))
                .toList();
    }
}
