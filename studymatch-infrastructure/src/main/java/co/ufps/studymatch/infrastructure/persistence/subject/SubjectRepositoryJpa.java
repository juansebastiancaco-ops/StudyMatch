package co.ufps.studymatch.infrastructure.persistence.subject;

import co.ufps.studymatch.subject.ISubjectRepository;
import co.ufps.studymatch.subject.Subject;
import co.ufps.studymatch.subject.SubjectId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class SubjectRepositoryJpa implements ISubjectRepository {
    @PersistenceContext
    EntityManager entityManager;

    @Override
    @Transactional
    public Subject guardar(Subject subject) {
        entityManager.merge(toEntity(subject));
        return subject;
    }

    @Override
    public Optional<Subject> buscarId(SubjectId subjectId) {
        SubjectJpa entity = entityManager.find(SubjectJpa.class, subjectId.value());
        return entity == null ? Optional.empty() : Optional.of(toDomain(entity));
    }

    @Override
    public List<Subject> buscar() {
        return entityManager.createQuery(
                        "SELECT s FROM SubjectJpa s WHERE s.active = true ORDER BY s.name ASC", SubjectJpa.class)
                .getResultList().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Subject> buscarPorIds(Collection<SubjectId> subjectIds) {
        if (subjectIds == null || subjectIds.isEmpty()) {
            return List.of();
        }
        List<String> ids = subjectIds.stream().map(SubjectId::value).toList();
        return entityManager.createQuery(
                        "SELECT s FROM SubjectJpa s WHERE s.subjectId IN :ids ORDER BY s.name ASC", SubjectJpa.class)
                .setParameter("ids", ids)
                .getResultList().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Subject> buscarPorTexto(String texto) {
        String query = "%" + texto.strip().toLowerCase() + "%";
        return entityManager.createQuery(
                        "SELECT s FROM SubjectJpa s WHERE s.active = true AND (LOWER(s.name) LIKE :texto "
                                + "OR LOWER(s.code) LIKE :texto OR LOWER(s.area) LIKE :texto) ORDER BY s.name ASC",
                        SubjectJpa.class)
                .setParameter("texto", query)
                .getResultList().stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existeCodigo(String code) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(s) FROM SubjectJpa s WHERE LOWER(s.code) = LOWER(:code)", Long.class)
                .setParameter("code", code)
                .getSingleResult();
        return count > 0;
    }

    private SubjectJpa toEntity(Subject subject) {
        SubjectJpa entity = new SubjectJpa();
        entity.setSubjectId(subject.getSubjectId().value());
        entity.setCode(subject.getCode());
        entity.setName(subject.getName());
        entity.setArea(subject.getArea());
        entity.setCredits(subject.getCredits());
        entity.setSemester(subject.getSemester());
        entity.setActive(subject.isActive());
        return entity;
    }

    private Subject toDomain(SubjectJpa entity) {
        return Subject.of(
                SubjectId.of(entity.getSubjectId()),
                entity.getCode(),
                entity.getName(),
                entity.getArea(),
                entity.getCredits(),
                entity.getSemester(),
                entity.isActive()
        );
    }
}
