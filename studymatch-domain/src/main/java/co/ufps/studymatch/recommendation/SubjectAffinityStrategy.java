package co.ufps.studymatch.recommendation;

import co.ufps.studymatch.subject.SubjectId;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SubjectAffinityStrategy implements RecommendationStrategy {
    public static final String NAME = "subject";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String description() {
        return "Asignaturas en comun entre el estudiante y el candidato o el grupo";
    }

    @Override
    public RecommendationScore score(RecommendationContext context) {
        if (context.group() != null) {
            return context.source().hasSubject(context.group().getSubjectId())
                    ? RecommendationScore.of(100)
                    : RecommendationScore.zero();
        }
        if (context.candidate() == null) {
            return RecommendationScore.zero();
        }
        List<SubjectId> sourceSubjects = context.source().getSubjectIds();
        if (sourceSubjects.isEmpty()) {
            return RecommendationScore.zero();
        }
        Set<SubjectId> common = new HashSet<>(context.source().getSubjectIds());
        common.retainAll(context.candidate().getSubjectIds());
        return RecommendationScore.of((common.size() * 100.0) / sourceSubjects.size());
    }
}
