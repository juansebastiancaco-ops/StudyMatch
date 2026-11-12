package co.ufps.studymatch.action;

import co.ufps.studymatch.student.StudentId;

import java.util.List;
import java.util.Optional;

public interface IActionHistoryRepository {
    ActionHistoryEntry guardar(ActionHistoryEntry entry);

    Optional<ActionHistoryEntry> buscarId(String actionId);

    List<ActionHistoryEntry> buscarPorEstudiante(StudentId studentId);

    void marcarDeshecha(String actionId);
}
