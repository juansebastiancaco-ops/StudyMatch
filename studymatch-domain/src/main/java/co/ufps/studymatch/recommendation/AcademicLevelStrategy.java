package co.ufps.studymatch.recommendation;

import co.ufps.studymatch.student.Student;

import java.util.List;

public final class AcademicLevelStrategy implements RecommendationStrategy {
    public static final String NAME = "academic";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String description() {
        return "Proximidad de semestre academico entre el estudiante y el candidato o los miembros del grupo";
    }

    @Override
    public RecommendationScore score(RecommendationContext context) {
        int reference = context.source().getSemester();
        if (context.group() != null) {
            List<Student> members = context.groupMembers();
            if (members.isEmpty()) {
                return RecommendationScore.of(50);
            }
            double average = members.stream().mapToInt(Student::getSemester).average().orElse(reference);
            return RecommendationScore.of(Math.max(0, 100 - Math.abs(average - reference) * 20));
        }
        if (context.candidate() == null) {
            return RecommendationScore.zero();
        }
        return RecommendationScore.of(Math.max(0,
                100 - Math.abs(context.candidate().getSemester() - reference) * 20));
    }
}
