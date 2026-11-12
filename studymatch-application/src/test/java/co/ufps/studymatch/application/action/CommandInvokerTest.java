package co.ufps.studymatch.application.action;

import co.ufps.studymatch.action.ActionHistoryEntry;
import co.ufps.studymatch.action.ActionHistoryNotFoundException;
import co.ufps.studymatch.action.IActionHistoryRepository;
import co.ufps.studymatch.application.common.ForbiddenOperationException;
import co.ufps.studymatch.student.StudentId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandInvokerTest {
    @Mock
    IActionHistoryRepository historyRepository;

    private CommandInvoker invoker;
    private final StudentId studentId = StudentId.generate();

    @BeforeEach
    void setUp() {
        invoker = new CommandInvoker(historyRepository);
    }

    @Test
    void registraComandoYDeshaceRestaurandoEstado() {
        FakeCommand command = new FakeCommand("action-1", studentId);
        invoker.registrar(command);
        ActionHistoryEntry entry = ActionHistoryEntry.create(
                command.actionId(), studentId, command.type(), command.description());
        when(historyRepository.buscarId(command.actionId())).thenReturn(Optional.of(entry));

        ActionHistoryDTO dto = invoker.deshacer(studentId, command.actionId());

        assertTrue(command.undone.get());
        assertTrue(dto.undone());
        assertFalse(dto.canUndo());
        verify(historyRepository).marcarDeshecha(command.actionId());
    }

    @Test
    void impideDeshacerAccionesDeOtroEstudiante() {
        FakeCommand command = new FakeCommand("action-2", studentId);
        invoker.registrar(command);

        assertThrows(ForbiddenOperationException.class,
                () -> invoker.deshacer(StudentId.generate(), command.actionId()));
        assertFalse(command.undone.get());
    }

    @Test
    void fallaSiLaAccionYaNoEstaEnMemoria() {
        assertThrows(ActionHistoryNotFoundException.class,
                () -> invoker.deshacer(studentId, "accion-inexistente"));
    }

    @Test
    void consultaElHistorialPersistidoYSuDisponibilidadDeUndo() {
        FakeCommand command = new FakeCommand("action-3", studentId);
        invoker.registrar(command);
        ActionHistoryEntry entry = ActionHistoryEntry.create(command.actionId(), studentId,
                command.type(), command.description());
        when(historyRepository.buscarPorEstudiante(studentId)).thenReturn(java.util.List.of(entry));

        var history = invoker.historial(studentId);

        assertTrue(history.get(0).canUndo());
        verify(historyRepository).buscarPorEstudiante(studentId);
        verify(historyRepository).guardar(any(ActionHistoryEntry.class));
    }

    private static final class FakeCommand implements ReversibleCommand {
        private final String actionId;
        private final StudentId studentId;
        private final AtomicBoolean undone = new AtomicBoolean(false);

        private FakeCommand(String actionId, StudentId studentId) {
            this.actionId = actionId;
            this.studentId = studentId;
        }

        @Override
        public String actionId() {
            return actionId;
        }

        @Override
        public StudentId studentId() {
            return studentId;
        }

        @Override
        public String type() {
            return "FAKE_ACTION";
        }

        @Override
        public String description() {
            return "Acción de prueba";
        }

        @Override
        public void execute() {
        }

        @Override
        public void undo() {
            undone.set(true);
        }
    }
}
