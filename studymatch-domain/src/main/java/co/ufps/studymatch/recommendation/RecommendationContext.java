package co.ufps.studymatch.recommendation;

import co.ufps.studymatch.group.StudyGroup;
import co.ufps.studymatch.student.Student;

import java.util.List;

/**
 * Contexto que reciben las estrategias. Sirve tanto para recomendar estudiantes como grupos.
 * Si candidate != null se evalua una persona; si group != null se evalua un grupo.
 */
public record RecommendationContext(
        Student source,
        Student candidate,
        StudyGroup group,
        List<Student> groupMembers,
        List<StudyGroup> sourceGroups,
        List<StudyGroup> candidateGroups
) {
    public static RecommendationContext forStudent(Student source, Student candidate,
                                                   List<StudyGroup> sourceGroups,
                                                   List<StudyGroup> candidateGroups) {
        return new RecommendationContext(source, candidate, null, List.of(), sourceGroups, candidateGroups);
    }

    public static RecommendationContext forGroup(Student source, StudyGroup group,
                                                 List<Student> groupMembers,
                                                 List<StudyGroup> sourceGroups) {
        return new RecommendationContext(source, null, group, groupMembers, sourceGroups, List.of());
    }
}
