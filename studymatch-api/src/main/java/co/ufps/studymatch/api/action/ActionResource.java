package co.ufps.studymatch.api.action;

import co.ufps.studymatch.api.security.CurrentStudent;
import co.ufps.studymatch.application.action.ActionHistoryDTO;
import co.ufps.studymatch.application.action.CommandInvoker;
import co.ufps.studymatch.application.action.GetActionHistoryQuery;
import co.ufps.studymatch.student.StudentId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

@Path("/api/v1/actions")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class ActionResource {
    private final GetActionHistoryQuery getActionHistoryQuery;
    private final CommandInvoker commandInvoker;

    @Inject
    public ActionResource(GetActionHistoryQuery getActionHistoryQuery, CommandInvoker commandInvoker) {
        this.getActionHistoryQuery = getActionHistoryQuery;
        this.commandInvoker = commandInvoker;
    }

    @GET
    @Path("/history")
    public List<ActionHistoryDTO> history(@Context SecurityContext securityContext,
                                          @QueryParam("studentId") String studentId) {
        return getActionHistoryQuery.execute(resolveStudentId(securityContext, studentId));
    }

    @POST
    @Path("/{actionId}/undo")
    @Consumes(MediaType.WILDCARD)
    public ActionHistoryDTO undo(@Context SecurityContext securityContext,
                                 @PathParam("actionId") String actionId) {
        return commandInvoker.deshacer(StudentId.of(CurrentStudent.id(securityContext)), actionId);
    }

    private String resolveStudentId(SecurityContext securityContext, String requestedStudentId) {
        String currentStudentId = CurrentStudent.id(securityContext);
        if (requestedStudentId != null && !requestedStudentId.isBlank()) {
            CurrentStudent.requireSelf(securityContext, requestedStudentId);
            return requestedStudentId;
        }
        return currentStudentId;
    }
}
