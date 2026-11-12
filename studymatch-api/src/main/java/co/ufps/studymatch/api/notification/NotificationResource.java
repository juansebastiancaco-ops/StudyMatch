package co.ufps.studymatch.api.notification;

import co.ufps.studymatch.api.security.CurrentStudent;
import co.ufps.studymatch.application.notification.CountUnreadNotificationsQuery;
import co.ufps.studymatch.application.notification.GetNotificationsQuery;
import co.ufps.studymatch.application.notification.MarkAllNotificationsReadCommand;
import co.ufps.studymatch.application.notification.MarkAllNotificationsReadHandler;
import co.ufps.studymatch.application.notification.MarkNotificationReadCommand;
import co.ufps.studymatch.application.notification.MarkNotificationReadHandler;
import co.ufps.studymatch.application.notification.NotificationDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

@Path("/api/v1/notifications")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class NotificationResource {
    private final GetNotificationsQuery getNotificationsQuery;
    private final CountUnreadNotificationsQuery countUnreadNotificationsQuery;
    private final MarkNotificationReadHandler markNotificationReadHandler;
    private final MarkAllNotificationsReadHandler markAllNotificationsReadHandler;

    @Inject
    public NotificationResource(GetNotificationsQuery getNotificationsQuery,
                                CountUnreadNotificationsQuery countUnreadNotificationsQuery,
                                MarkNotificationReadHandler markNotificationReadHandler,
                                MarkAllNotificationsReadHandler markAllNotificationsReadHandler) {
        this.getNotificationsQuery = getNotificationsQuery;
        this.countUnreadNotificationsQuery = countUnreadNotificationsQuery;
        this.markNotificationReadHandler = markNotificationReadHandler;
        this.markAllNotificationsReadHandler = markAllNotificationsReadHandler;
    }

    @GET
    public List<NotificationDTO> list(@Context SecurityContext securityContext,
                                      @QueryParam("studentId") String studentId,
                                      @QueryParam("unreadOnly") boolean unreadOnly) {
        return getNotificationsQuery.execute(resolveStudentId(securityContext, studentId), unreadOnly);
    }

    @GET
    @Path("/unread-count")
    public UnreadCountDTO unreadCount(@Context SecurityContext securityContext,
                                      @QueryParam("studentId") String studentId) {
        return new UnreadCountDTO(countUnreadNotificationsQuery.execute(
                resolveStudentId(securityContext, studentId)));
    }

    @PUT
    @Path("/{notificationId}/read")
    @Consumes(MediaType.WILDCARD)
    public NotificationDTO markRead(@Context SecurityContext securityContext,
                                    @PathParam("notificationId") String notificationId,
                                    @QueryParam("studentId") String studentId) {
        return markNotificationReadHandler.execute(new MarkNotificationReadCommand(
                resolveStudentId(securityContext, studentId),
                notificationId
        ));
    }

    @POST
    @Path("/read-all")
    @Consumes(MediaType.WILDCARD)
    public UnreadCountDTO markAllRead(@Context SecurityContext securityContext,
                                      @QueryParam("studentId") String studentId) {
        long remaining = markAllNotificationsReadHandler.execute(new MarkAllNotificationsReadCommand(
                resolveStudentId(securityContext, studentId)));
        return new UnreadCountDTO(remaining);
    }

    private String resolveStudentId(SecurityContext securityContext, String requestedStudentId) {
        String currentStudentId = CurrentStudent.id(securityContext);
        if (requestedStudentId != null && !requestedStudentId.isBlank()) {
            CurrentStudent.requireSelf(securityContext, requestedStudentId);
            return requestedStudentId;
        }
        return currentStudentId;
    }

    public record UnreadCountDTO(long count) {
    }
}
