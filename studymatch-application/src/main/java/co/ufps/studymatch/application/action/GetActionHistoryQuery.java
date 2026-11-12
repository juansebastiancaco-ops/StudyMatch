package co.ufps.studymatch.application.action;

import co.ufps.studymatch.student.StudentId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class GetActionHistoryQuery {
    private final CommandInvoker commandInvoker;

    @Inject
    public GetActionHistoryQuery(CommandInvoker commandInvoker) {
        this.commandInvoker = commandInvoker;
    }

    public List<ActionHistoryDTO> execute(String studentId) {
        return commandInvoker.historial(StudentId.of(studentId));
    }
}
