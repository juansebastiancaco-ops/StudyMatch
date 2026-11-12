package co.ufps.studymatch.application.student;

import co.ufps.studymatch.application.subject.SubjectDTO;
import co.ufps.studymatch.group.GroupStatus;
import co.ufps.studymatch.group.IStudyGroupRepository;
import co.ufps.studymatch.group.StudyGroup;
import co.ufps.studymatch.notification.INotificationRepository;
import co.ufps.studymatch.student.AcademicProfile;
import co.ufps.studymatch.student.Student;
import co.ufps.studymatch.subject.ISubjectRepository;
import co.ufps.studymatch.subject.Subject;
import co.ufps.studymatch.subject.SubjectId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class StudentDtoAssembler {
    private final ISubjectRepository subjectRepository;
    private final IStudyGroupRepository groupRepository;
    private final INotificationRepository notificationRepository;

    @Inject
    public StudentDtoAssembler(ISubjectRepository subjectRepository,
                               IStudyGroupRepository groupRepository,
                               INotificationRepository notificationRepository) {
        this.subjectRepository = subjectRepository;
        this.groupRepository = groupRepository;
        this.notificationRepository = notificationRepository;
    }

    public StudentDTO toDto(Student student) {
        Map<SubjectId, Subject> subjects = subjectMap(student.getSubjectIds());
        AcademicProfile profile = student.getAcademicProfile();
        List<StudyGroup> activeGroups = groupRepository.buscarPorEstudiante(student.getStudentId()).stream()
                .filter(group -> group.getStatus().isActive())
                .toList();
        int owned = (int) activeGroups.stream()
                .filter(group -> group.getCreatorId().equals(student.getStudentId()))
                .count();
        StudentStatsDTO stats = new StudentStatsDTO(
                activeGroups.size(),
                owned,
                groupRepository.contarSolicitudesPendientes(student.getStudentId()),
                groupRepository.contarSolicitudesRecibidasPendientes(student.getStudentId()),
                notificationRepository.contarNoLeidas(student.getStudentId())
        );
        AcademicProfileDTO profileDTO = new AcademicProfileDTO(
                profile.getProfileId().value(),
                profile.getBio(),
                profile.getSubjects().stream()
                        .map(subjects::get)
                        .filter(java.util.Objects::nonNull)
                        .map(SubjectDTO::from)
                        .toList(),
                profile.getAvailabilityStatus().name(),
                profile.getAvailabilitySlots().stream().map(AvailabilitySlotDTO::from).toList()
        );
        return new StudentDTO(
                student.getStudentId().value(),
                student.getEmail(),
                student.getFullName(),
                student.getCareer(),
                student.getSemester(),
                student.getStatus().name(),
                student.getCreatedAt(),
                profileDTO,
                stats
        );
    }

    public StudentSummaryDTO toSummary(Student student,
                                       Double affinityScore,
                                       List<String> reasons,
                                       List<String> sharedSubjects) {
        return toSummary(student, subjectMap(student.getSubjectIds()), affinityScore, reasons, sharedSubjects);
    }

    public StudentSummaryDTO toSummary(Student student,
                                       Map<SubjectId, Subject> subjects,
                                       Double affinityScore,
                                       List<String> reasons,
                                       List<String> sharedSubjects) {
        return new StudentSummaryDTO(
                student.getStudentId().value(),
                student.getFullName(),
                student.getCareer(),
                student.getSemester(),
                student.getAcademicProfile().getBio(),
                student.getSubjectIds().stream()
                        .map(subjects::get)
                        .filter(java.util.Objects::nonNull)
                        .map(SubjectDTO::from)
                        .toList(),
                student.getAvailabilityStatus().name(),
                student.getAvailabilitySlots().stream().map(AvailabilitySlotDTO::from).toList(),
                affinityScore,
                reasons == null ? List.of() : List.copyOf(reasons),
                sharedSubjects == null ? List.of() : List.copyOf(sharedSubjects)
        );
    }

    public Map<SubjectId, Subject> subjectMap(List<SubjectId> subjectIds) {
        Map<SubjectId, Subject> result = new LinkedHashMap<>();
        subjectRepository.buscarPorIds(subjectIds).forEach(subject -> result.put(subject.getSubjectId(), subject));
        return result;
    }
}
