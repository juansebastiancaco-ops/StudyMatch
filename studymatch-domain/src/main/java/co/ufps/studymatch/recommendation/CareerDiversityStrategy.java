package co.ufps.studymatch.recommendation;

import co.ufps.studymatch.student.Student;

public final class CareerDiversityStrategy implements RecommendationStrategy {
    public static final String NAME = "career";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String description() {
        return "Fomenta la diversidad de carreras en los grupos interdisciplinarios";
    }

    @Override
    public RecommendationScore score(RecommendationContext context) {
        if (context.group() != null) {
            if (context.groupMembers().isEmpty()) {
                return RecommendationScore.of(100);
            }
            long different = context.groupMembers().stream()
                    .filter(member -> !member.getCareer().equalsIgnoreCase(context.source().getCareer()))
                    .count();
            return RecommendationScore.of((different * 100.0) / context.groupMembers().size());
        }
        if (context.candidate() == null) {
            return RecommendationScore.zero();
        }
        return context.candidate().getCareer().equalsIgnoreCase(context.source().getCareer())
                ? RecommendationScore.of(40)
                : RecommendationScore.of(100);
    }
}
