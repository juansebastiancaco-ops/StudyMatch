package co.ufps.studymatch.group;

import co.ufps.studymatch.student.StudentId;
import co.ufps.studymatch.subject.SubjectId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IStudyGroupRepository {
    StudyGroup guardar(StudyGroup group);

    Optional<StudyGroup> buscarId(StudyGroupId groupId);

    List<StudyGroup> buscar(SubjectId subjectId,
                            GroupStatus status,
                            String texto,
                            GroupModality modality,
                            int offset,
                            int limit);

    long contar(SubjectId subjectId, GroupStatus status, String texto, GroupModality modality);

    List<StudyGroup> buscarTodos();

    List<StudyGroup> buscarPorIds(Collection<StudyGroupId> groupIds);

    List<StudyGroup> buscarPorEstudiante(StudentId studentId);

    long contarGruposActivos(StudentId studentId);

    long contarSolicitudesPendientes(StudentId studentId);

    long contarSolicitudesRecibidasPendientes(StudentId studentId);

    int borrar(StudyGroupId groupId);
}
