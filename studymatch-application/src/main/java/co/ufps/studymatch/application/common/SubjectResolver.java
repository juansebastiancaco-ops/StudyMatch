package co.ufps.studymatch.application.common;

import co.ufps.studymatch.ValidationException;
import co.ufps.studymatch.subject.ISubjectRepository;
import co.ufps.studymatch.subject.Subject;
import co.ufps.studymatch.subject.SubjectId;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class SubjectResolver {
    private SubjectResolver() {
    }

    public static List<SubjectId> resolve(ISubjectRepository repository, List<String> rawSubjectIds) {
        if (rawSubjectIds == null || rawSubjectIds.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<SubjectId> ids = new LinkedHashSet<>();
        for (String raw : rawSubjectIds) {
            if (raw == null || raw.isBlank()) {
                throw new ValidationException("Los subjectId no pueden estar vacios");
            }
            ids.add(SubjectId.of(raw));
        }
        List<Subject> found = repository.buscarPorIds(ids);
        Set<SubjectId> foundIds = found.stream().map(Subject::getSubjectId).collect(java.util.stream.Collectors.toSet());
        SubjectId missing = ids.stream().filter(id -> !foundIds.contains(id)).findFirst().orElse(null);
        if (missing != null) {
            throw new SubjectNotFoundException(missing.value());
        }
        return List.copyOf(ids);
    }
}
