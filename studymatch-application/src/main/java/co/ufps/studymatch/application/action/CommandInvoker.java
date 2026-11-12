package co.ufps.studymatch.application.action;

import co.ufps.studymatch.action.ActionHistoryEntry;
import co.ufps.studymatch.action.ActionHistoryNotFoundException;
import co.ufps.studymatch.action.IActionHistoryRepository;
import co.ufps.studymatch.application.common.ForbiddenOperationException;
import co.ufps.studymatch.student.StudentId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Invoker del patron Command. Mantiene una pila por estudiante y persiste la auditoria.
 * Los comandos viven en memoria: despues de reiniciar se puede consultar el historial,
 * pero canUndo sera false. Esta limitacion esta documentada en ARQUITECTURA.md.
 */
@ApplicationScoped
public class CommandInvoker {
    private static final Logger LOGGER = Logger.getLogger(CommandInvoker.class.getName());
    private static final int MAX_ACTIONS_PER_STUDENT = 50;

    private final Map<String, ReversibleCommand> commands = new ConcurrentHashMap<>();
    private final Map<StudentId, ConcurrentLinkedDeque<String>> commandsByStudent = new ConcurrentHashMap<>();
    private final IActionHistoryRepository historyRepository;

    @Inject
    public CommandInvoker(IActionHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public void registrar(ReversibleCommand command) {
        commands.put(command.actionId(), command);
        ConcurrentLinkedDeque<String> stack = commandsByStudent.computeIfAbsent(
                command.studentId(), key -> new ConcurrentLinkedDeque<>());
        stack.addFirst(command.actionId());
        while (stack.size() > MAX_ACTIONS_PER_STUDENT) {
            String oldest = stack.removeLast();
            commands.remove(oldest);
        }
        try {
            historyRepository.guardar(ActionHistoryEntry.create(
                    command.actionId(),
                    command.studentId(),
                    command.type(),
                    command.description()
            ));
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "No se pudo persistir el historial de la accion " + command.actionId(), e);
        }
    }

    public ActionHistoryDTO deshacer(StudentId studentId, String actionId) {
        ReversibleCommand command = commands.get(actionId);
        if (command == null) {
            throw new ActionHistoryNotFoundException(actionId);
        }
        if (!command.studentId().equals(studentId)) {
            throw new ForbiddenOperationException("No puedes deshacer una accion de otro estudiante");
        }
        command.undo();
        commands.remove(actionId);
        ConcurrentLinkedDeque<String> stack = commandsByStudent.get(studentId);
        if (stack != null) {
            stack.remove(actionId);
        }
        try {
            historyRepository.marcarDeshecha(actionId);
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "No se pudo marcar como deshecha la accion " + actionId, e);
        }
        ActionHistoryEntry entry = historyRepository.buscarId(actionId)
                .orElse(ActionHistoryEntry.create(actionId, studentId, command.type(), command.description()));
        entry.markUndone();
        return ActionHistoryDTO.from(entry, false);
    }

    public List<ActionHistoryDTO> historial(StudentId studentId) {
        List<ActionHistoryEntry> entries = new ArrayList<>(historyRepository.buscarPorEstudiante(studentId));
        return entries.stream()
                .map(entry -> ActionHistoryDTO.from(entry, commands.containsKey(entry.getActionId())))
                .toList();
    }

    public boolean puedeDeshacer(StudentId studentId, String actionId) {
        ReversibleCommand command = commands.get(actionId);
        return command != null && command.studentId().equals(studentId);
    }
}
