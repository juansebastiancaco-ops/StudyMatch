package co.ufps.studymatch.subject;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ISubjectRepository {
    Subject guardar(Subject subject);

    Optional<Subject> buscarId(SubjectId subjectId);

    List<Subject> buscar();

    List<Subject> buscarPorIds(Collection<SubjectId> subjectIds);

    List<Subject> buscarPorTexto(String texto);

    boolean existeCodigo(String code);
}
