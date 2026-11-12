package co.ufps.studymatch.application.recommendation;

import co.ufps.studymatch.application.group.GroupDtoAssembler;
import co.ufps.studymatch.application.group.StudyGroupSummaryDTO;
import co.ufps.studymatch.application.student.StudentDtoAssembler;
import co.ufps.studymatch.application.student.StudentSummaryDTO;
import co.ufps.studymatch.group.GroupStatus;
import co.ufps.studymatch.group.IStudyGroupRepository;
import co.ufps.studymatch.group.StudyGroup;
import co.ufps.studymatch.recommendation.RecommendationContext;
import co.ufps.studymatch.recommendation.RecommendationScore;
import co.ufps.studymatch.recommendation.RecommendationStrategy;
import co.ufps.studymatch.student.IStudentRepository;
import co.ufps.studymatch.student.Student;
import co.ufps.studymatch.student.StudentId;
import co.ufps.studymatch.student.StudentNotFoundException;
import co.ufps.studymatch.subject.ISubjectRepository;
import co.ufps.studymatch.subject.Subject;
import co.ufps.studymatch.subject.SubjectId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class RecommendationService {
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 50;

    private final IStudentRepository studentRepository;
    private final IStudyGroupRepository groupRepository;
    private final ISubjectRepository subjectRepository;
    private final RecommendationStrategyRegistry registry;
    private final StudentDtoAssembler studentAssembler;
    private final GroupDtoAssembler groupAssembler;
    private final RecommendationCache cache;

    @Inject
    public RecommendationService(IStudentRepository studentRepository,
                                 IStudyGroupRepository groupRepository,
                                 ISubjectRepository subjectRepository,
                                 RecommendationStrategyRegistry registry,
                                 StudentDtoAssembler studentAssembler,
                                 GroupDtoAssembler groupAssembler,
                                 RecommendationCache cache) {
        this.studentRepository = studentRepository;
        this.groupRepository = groupRepository;
        this.subjectRepository = subjectRepository;
        this.registry = registry;
        this.studentAssembler = studentAssembler;
        this.groupAssembler = groupAssembler;
        this.cache = cache;
    }

    public List<StudentSummaryDTO> recommendStudents(String studentId, String strategyName, int limit) {
        Student source = studentRepository.buscarId(StudentId.of(studentId))
                .orElseThrow(() -> new StudentNotFoundException(studentId));
        RecommendationStrategy strategy = registry.get(strategyName);
        int safeLimit = safeLimit(limit);

        List<Student> candidates = studentRepository.buscarTodos();
        Map<SubjectId, Subject> subjects = subjectMap();
        List<StudyGroup> sourceGroups = groupRepository.buscarPorEstudiante(source.getStudentId());

        List<StudentSummaryDTO> results = new ArrayList<>();
        for (Student candidate : candidates) {
            if (candidate.getStudentId().equals(source.getStudentId()) || !candidate.isActive()) {
                continue;
            }
            RecommendationContext context = RecommendationContext.forStudent(
                    source, candidate, sourceGroups, List.of());
            RecommendationScore score = strategy.score(context);
            if (score.value() <= 0) {
                continue;
            }
            List<String> shared = source.sharedSubjectsWith(candidate).stream()
                    .map(subjects::get)
                    .filter(java.util.Objects::nonNull)
                    .map(Subject::getName)
                    .toList();
            results.add(studentAssembler.toSummary(candidate, subjects, score.value(),
                    reasonsFor(strategyName, context), shared));
        }
        return results.stream()
                .sorted(Comparator.comparing(StudentSummaryDTO::affinityScore,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(safeLimit)
                .toList();
    }

    public List<StudyGroupSummaryDTO> recommendGroups(String studentId, String strategyName, int limit) {
        Student source = studentRepository.buscarId(StudentId.of(studentId))
                .orElseThrow(() -> new StudentNotFoundException(studentId));
        RecommendationStrategy strategy = registry.get(strategyName);
        int safeLimit = safeLimit(limit);

        List<Student> allStudents = studentRepository.buscarTodos();
        Map<StudentId, Student> studentsById = allStudents.stream()
                .collect(Collectors.toMap(Student::getStudentId, Function.identity()));
        List<StudyGroup> sourceGroups = groupRepository.buscarPorEstudiante(source.getStudentId());

        List<StudyGroupSummaryDTO> results = new ArrayList<>();
        for (StudyGroup group : groupRepository.buscarTodos()) {
            if (group.getStatus() != GroupStatus.OPEN || group.isMember(source.getStudentId())) {
                continue;
            }
            List<Student> members = group.getMemberIds().stream()
                    .map(studentsById::get)
                    .filter(java.util.Objects::nonNull)
                    .toList();
            RecommendationContext context = RecommendationContext.forGroup(source, group, members, sourceGroups);
            RecommendationScore score = strategy.score(context);
            if (score.value() <= 0) {
                continue;
            }
            results.add(groupAssembler.toSummary(group, score.value(), reasonsFor(strategyName, context)));
        }
        return results.stream()
                .sorted(Comparator.comparing(StudyGroupSummaryDTO::affinityScore,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(safeLimit)
                .toList();
    }

    /**
     * Worker: recalcula la matriz de afinidad estudiante-grupo en la cache thread-safe.
     */
    public int refreshAffinityScores() {
        RecommendationStrategy composite = registry.get("composite");
        List<Student> students = studentRepository.buscarTodos().stream().filter(Student::isActive).toList();
        List<StudyGroup> groups = groupRepository.buscarTodos();
        Map<StudentId, Student> studentsById = students.stream()
                .collect(Collectors.toMap(Student::getStudentId, Function.identity()));
        Map<StudentId, List<StudyGroup>> groupsByMember = new LinkedHashMap<>();
        for (StudyGroup group : groups) {
            for (StudentId memberId : group.getMemberIds()) {
                groupsByMember.computeIfAbsent(memberId, key -> new ArrayList<>()).add(group);
            }
        }
        int computed = 0;
        for (Student student : students) {
            List<StudyGroup> sourceGroups = groupsByMember.getOrDefault(student.getStudentId(), List.of());
            for (StudyGroup group : groups) {
                if (group.isMember(student.getStudentId())) {
                    continue;
                }
                List<Student> members = group.getMemberIds().stream()
                        .map(studentsById::get)
                        .filter(java.util.Objects::nonNull)
                        .toList();
                double score = composite.score(RecommendationContext.forGroup(student, group, members, sourceGroups)).value();
                cache.putAffinity(student.getStudentId().value(), group.getGroupId().value(), score);
                computed++;
            }
        }
        cache.markRefreshed();
        return computed;
    }

    /**
     * Worker: recalcula los top de estudiantes y grupos para cada estudiante.
     */
    public int refreshRecommendations() {
        List<Student> students = studentRepository.buscarTodos().stream().filter(Student::isActive).toList();
        for (Student student : students) {
            cache.putStudentRecommendations(student.getStudentId().value(),
                    recommendStudents(student.getStudentId().value(), "composite", 5));
            cache.putGroupRecommendations(student.getStudentId().value(),
                    recommendGroups(student.getStudentId().value(), "composite", 5));
        }
        cache.markRefreshed();
        return students.size();
    }

    private int safeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private Map<SubjectId, Subject> subjectMap() {
        Map<SubjectId, Subject> subjects = new LinkedHashMap<>();
        subjectRepository.buscar().forEach(subject -> subjects.put(subject.getSubjectId(), subject));
        return subjects;
    }

    private List<String> reasonsFor(String strategyName, RecommendationContext context) {
        List<RecommendationStrategy> components = registry.componentsFor(strategyName);
        record ScoredReason(RecommendationStrategy strategy, double score) {
        }
        return components.stream()
                .map(strategy -> new ScoredReason(strategy, strategy.score(context).value()))
                .filter(reason -> reason.score() > 0)
                .sorted(Comparator.comparingDouble(ScoredReason::score).reversed())
                .limit(3)
                .map(reason -> reason.strategy().description() + " ("
                        + RecommendationScore.of(reason.score()).formatted() + ")")
                .toList();
    }
}
