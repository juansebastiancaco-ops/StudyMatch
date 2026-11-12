package co.ufps.studymatch.application.subject;

import co.ufps.studymatch.subject.ISubjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class SearchSubjectsQuery {
    private final ISubjectRepository subjectRepository;

    @Inject
    public SearchSubjectsQuery(ISubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public List<SubjectDTO> execute(String texto) {
        List<co.ufps.studymatch.subject.Subject> subjects = texto == null || texto.isBlank()
                ? subjectRepository.buscar()
                : subjectRepository.buscarPorTexto(texto);
        return subjects.stream().map(SubjectDTO::from).toList();
    }
}
