package co.ufps.studymatch.api.group;

import co.ufps.studymatch.api.security.CurrentStudent;
import co.ufps.studymatch.application.common.AvailabilitySlotInput;
import co.ufps.studymatch.application.common.PageResult;
import co.ufps.studymatch.application.group.AcceptJoinRequestCommand;
import co.ufps.studymatch.application.group.AcceptJoinRequestHandler;
import co.ufps.studymatch.application.group.CancelJoinRequestCommand;
import co.ufps.studymatch.application.group.CancelJoinRequestHandler;
import co.ufps.studymatch.application.group.ChangeGroupStatusCommand;
import co.ufps.studymatch.application.group.ChangeGroupStatusHandler;
import co.ufps.studymatch.application.group.CreateGroupCommand;
import co.ufps.studymatch.application.group.CreateGroupHandler;
import co.ufps.studymatch.application.group.GetGroupQuery;
import co.ufps.studymatch.application.group.GetGroupRequestsQuery;
import co.ufps.studymatch.application.group.JoinRequestDTO;
import co.ufps.studymatch.application.group.LeaveGroupCommand;
import co.ufps.studymatch.application.group.LeaveGroupHandler;
import co.ufps.studymatch.application.group.RejectJoinRequestCommand;
import co.ufps.studymatch.application.group.RejectJoinRequestHandler;
import co.ufps.studymatch.application.group.SearchGroupsQuery;
import co.ufps.studymatch.application.group.SendJoinRequestCommand;
import co.ufps.studymatch.application.group.SendJoinRequestHandler;
import co.ufps.studymatch.application.group.StudyGroupDetailDTO;
import co.ufps.studymatch.application.group.StudyGroupSummaryDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.List;

@Path("/api/v1/groups")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GroupResource {
    private final CreateGroupHandler createGroupHandler;
    private final SearchGroupsQuery searchGroupsQuery;
    private final GetGroupQuery getGroupQuery;
    private final ChangeGroupStatusHandler changeGroupStatusHandler;
    private final SendJoinRequestHandler sendJoinRequestHandler;
    private final AcceptJoinRequestHandler acceptJoinRequestHandler;
    private final RejectJoinRequestHandler rejectJoinRequestHandler;
    private final CancelJoinRequestHandler cancelJoinRequestHandler;
    private final LeaveGroupHandler leaveGroupHandler;
    private final GetGroupRequestsQuery getGroupRequestsQuery;

    @Inject
    public GroupResource(CreateGroupHandler createGroupHandler,
                         SearchGroupsQuery searchGroupsQuery,
                         GetGroupQuery getGroupQuery,
                         ChangeGroupStatusHandler changeGroupStatusHandler,
                         SendJoinRequestHandler sendJoinRequestHandler,
                         AcceptJoinRequestHandler acceptJoinRequestHandler,
                         RejectJoinRequestHandler rejectJoinRequestHandler,
                         CancelJoinRequestHandler cancelJoinRequestHandler,
                         LeaveGroupHandler leaveGroupHandler,
                         GetGroupRequestsQuery getGroupRequestsQuery) {
        this.createGroupHandler = createGroupHandler;
        this.searchGroupsQuery = searchGroupsQuery;
        this.getGroupQuery = getGroupQuery;
        this.changeGroupStatusHandler = changeGroupStatusHandler;
        this.sendJoinRequestHandler = sendJoinRequestHandler;
        this.acceptJoinRequestHandler = acceptJoinRequestHandler;
        this.rejectJoinRequestHandler = rejectJoinRequestHandler;
        this.cancelJoinRequestHandler = cancelJoinRequestHandler;
        this.leaveGroupHandler = leaveGroupHandler;
        this.getGroupRequestsQuery = getGroupRequestsQuery;
    }

    @POST
    public Response create(@Context SecurityContext securityContext,
                           @Context UriInfo uriInfo,
                           CreateGroupRequest request) {
        int maxMembers = request.maxMembers() == null || request.maxMembers() <= 0
                ? 6
                : request.maxMembers();
        StudyGroupDetailDTO created = createGroupHandler.execute(new CreateGroupCommand(
                CurrentStudent.id(securityContext),
                request.subjectId(),
                request.name(),
                request.description(),
                maxMembers,
                request.modality(),
                request.schedule() == null ? List.of() : request.schedule()
        ));
        URI location = uriInfo.getAbsolutePathBuilder().path(created.groupId()).build();
        return Response.created(location).entity(created).build();
    }

    @GET
    public PageResult<StudyGroupSummaryDTO> search(@QueryParam("subject") String subject,
                                                   @QueryParam("subjectId") String subjectId,
                                                   @QueryParam("status") String status,
                                                   @QueryParam("q") String q,
                                                   @QueryParam("modality") String modality,
                                                   @QueryParam("page") int page,
                                                   @QueryParam("size") int size) {
        String subjectFilter = subject == null || subject.isBlank() ? subjectId : subject;
        return searchGroupsQuery.execute(subjectFilter, status, q, modality, page, size);
    }

    @GET
    @Path("/{groupId}")
    public StudyGroupDetailDTO detail(@Context SecurityContext securityContext,
                                      @PathParam("groupId") String groupId) {
        return getGroupQuery.execute(groupId, CurrentStudent.id(securityContext));
    }

    @PUT
    @Path("/{groupId}/status")
    public StudyGroupDetailDTO changeStatus(@Context SecurityContext securityContext,
                                            @PathParam("groupId") String groupId,
                                            ChangeGroupStatusRequest request) {
        return changeGroupStatusHandler.execute(new ChangeGroupStatusCommand(
                CurrentStudent.id(securityContext),
                groupId,
                request.status()
        ));
    }

    @GET
    @Path("/{groupId}/requests")
    public List<JoinRequestDTO> requests(@Context SecurityContext securityContext,
                                         @PathParam("groupId") String groupId) {
        return getGroupRequestsQuery.execute(groupId, CurrentStudent.id(securityContext));
    }

    @POST
    @Path("/{groupId}/requests")
    public Response sendRequest(@Context SecurityContext securityContext,
                                @PathParam("groupId") String groupId,
                                SendJoinRequestRequest request) {
        JoinRequestDTO joinRequest = sendJoinRequestHandler.execute(new SendJoinRequestCommand(
                CurrentStudent.id(securityContext),
                groupId,
                request == null ? null : request.message()
        ));
        return Response.status(Response.Status.CREATED).entity(joinRequest).build();
    }

    @POST
    @Path("/{groupId}/requests/{requestId}/accept")
    @Consumes(MediaType.WILDCARD)
    public JoinRequestDTO accept(@Context SecurityContext securityContext,
                                 @PathParam("groupId") String groupId,
                                 @PathParam("requestId") String requestId) {
        return acceptJoinRequestHandler.execute(new AcceptJoinRequestCommand(
                CurrentStudent.id(securityContext),
                groupId,
                requestId
        ));
    }

    @POST
    @Path("/{groupId}/requests/{requestId}/reject")
    @Consumes(MediaType.WILDCARD)
    public JoinRequestDTO reject(@Context SecurityContext securityContext,
                                 @PathParam("groupId") String groupId,
                                 @PathParam("requestId") String requestId) {
        return rejectJoinRequestHandler.execute(new RejectJoinRequestCommand(
                CurrentStudent.id(securityContext),
                groupId,
                requestId
        ));
    }

    @DELETE
    @Path("/{groupId}/requests/{requestId}")
    @Consumes(MediaType.WILDCARD)
    public JoinRequestDTO cancel(@Context SecurityContext securityContext,
                                 @PathParam("groupId") String groupId,
                                 @PathParam("requestId") String requestId) {
        return cancelJoinRequestHandler.execute(new CancelJoinRequestCommand(
                CurrentStudent.id(securityContext),
                groupId,
                requestId
        ));
    }

    @DELETE
    @Path("/{groupId}/members/{studentId}")
    @Consumes(MediaType.WILDCARD)
    public Response leave(@Context SecurityContext securityContext,
                          @PathParam("groupId") String groupId,
                          @PathParam("studentId") String studentId) {
        CurrentStudent.requireSelf(securityContext, studentId);
        leaveGroupHandler.execute(new LeaveGroupCommand(studentId, groupId));
        return Response.noContent().build();
    }

    public record CreateGroupRequest(
            String subjectId,
            String name,
            String description,
            Integer maxMembers,
            String modality,
            List<AvailabilitySlotInput> schedule
    ) {
    }

    public record ChangeGroupStatusRequest(String status) {
    }

    public record SendJoinRequestRequest(String message) {
    }
}
