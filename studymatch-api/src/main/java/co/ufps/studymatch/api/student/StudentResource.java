package co.ufps.studymatch.api.student;

import co.ufps.studymatch.api.security.CurrentStudent;
import co.ufps.studymatch.application.common.AvailabilitySlotInput;
import co.ufps.studymatch.application.group.StudyGroupSummaryDTO;
import co.ufps.studymatch.application.student.ChangeAvailabilityCommand;
import co.ufps.studymatch.application.student.ChangeAvailabilityHandler;
import co.ufps.studymatch.application.student.DashboardDTO;
import co.ufps.studymatch.application.student.GetDashboardQuery;
import co.ufps.studymatch.application.student.GetStudentCommand;
import co.ufps.studymatch.application.student.GetStudentGroupsQuery;
import co.ufps.studymatch.application.student.GetStudentHandler;
import co.ufps.studymatch.application.student.StudentDTO;
import co.ufps.studymatch.application.student.UpdateProfileCommand;
import co.ufps.studymatch.application.student.UpdateProfileHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

@Path("/api/v1/students")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StudentResource {
    private final GetStudentHandler getStudentHandler;
    private final UpdateProfileHandler updateProfileHandler;
    private final ChangeAvailabilityHandler changeAvailabilityHandler;
    private final GetStudentGroupsQuery getStudentGroupsQuery;
    private final GetDashboardQuery getDashboardQuery;

    @Inject
    public StudentResource(GetStudentHandler getStudentHandler,
                           UpdateProfileHandler updateProfileHandler,
                           ChangeAvailabilityHandler changeAvailabilityHandler,
                           GetStudentGroupsQuery getStudentGroupsQuery,
                           GetDashboardQuery getDashboardQuery) {
        this.getStudentHandler = getStudentHandler;
        this.updateProfileHandler = updateProfileHandler;
        this.changeAvailabilityHandler = changeAvailabilityHandler;
        this.getStudentGroupsQuery = getStudentGroupsQuery;
        this.getDashboardQuery = getDashboardQuery;
    }

    @GET
    @Path("/me")
    public StudentDTO me(@Context SecurityContext securityContext) {
        return getStudentHandler.execute(new GetStudentCommand(CurrentStudent.id(securityContext)));
    }

    @GET
    @Path("/me/dashboard")
    public DashboardDTO dashboard(@Context SecurityContext securityContext) {
        return getDashboardQuery.execute(CurrentStudent.id(securityContext));
    }

    @GET
    @Path("/me/groups")
    public List<StudyGroupSummaryDTO> myGroups(@Context SecurityContext securityContext) {
        return getStudentGroupsQuery.execute(CurrentStudent.id(securityContext));
    }

    @PUT
    @Path("/me/profile")
    public StudentDTO updateProfile(@Context SecurityContext securityContext, UpdateProfileRequest request) {
        return updateProfile(CurrentStudent.id(securityContext), request);
    }

    @PUT
    @Path("/me/availability")
    public StudentDTO changeAvailability(@Context SecurityContext securityContext,
                                         ChangeAvailabilityRequest request) {
        return changeAvailability(CurrentStudent.id(securityContext), request);
    }

    @PUT
    @Path("/{studentId}/profile")
    public StudentDTO updateProfileById(@Context SecurityContext securityContext,
                                        @PathParam("studentId") String studentId,
                                        UpdateProfileRequest request) {
        CurrentStudent.requireSelf(securityContext, studentId);
        return updateProfile(studentId, request);
    }

    @PUT
    @Path("/{studentId}/availability")
    public StudentDTO changeAvailabilityById(@Context SecurityContext securityContext,
                                             @PathParam("studentId") String studentId,
                                             ChangeAvailabilityRequest request) {
        CurrentStudent.requireSelf(securityContext, studentId);
        return changeAvailability(studentId, request);
    }

    @GET
    @Path("/{studentId}/groups")
    public List<StudyGroupSummaryDTO> groupsByStudent(@Context SecurityContext securityContext,
                                                      @PathParam("studentId") String studentId) {
        CurrentStudent.requireSelf(securityContext, studentId);
        return getStudentGroupsQuery.execute(studentId);
    }

    @GET
    @Path("/{studentId}")
    public StudentDTO byId(@PathParam("studentId") String studentId) {
        return getStudentHandler.execute(new GetStudentCommand(studentId));
    }

    private StudentDTO updateProfile(String studentId, UpdateProfileRequest request) {
        return updateProfileHandler.execute(new UpdateProfileCommand(
                studentId,
                request.career(),
                request.semester(),
                request.bio(),
                request.subjectIds()
        ));
    }

    private StudentDTO changeAvailability(String studentId, ChangeAvailabilityRequest request) {
        return changeAvailabilityHandler.execute(new ChangeAvailabilityCommand(
                studentId,
                request.availabilityStatus(),
                request.availabilitySlots()
        ));
    }

    public record UpdateProfileRequest(
            String career,
            int semester,
            String bio,
            List<String> subjectIds
    ) {
    }

    public record ChangeAvailabilityRequest(
            String availabilityStatus,
            List<AvailabilitySlotInput> availabilitySlots
    ) {
    }
}
