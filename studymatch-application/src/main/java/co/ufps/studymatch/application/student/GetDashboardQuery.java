package co.ufps.studymatch.application.student;

import co.ufps.studymatch.application.recommendation.RecommendationService;
import co.ufps.studymatch.group.IStudyGroupRepository;
import co.ufps.studymatch.notification.INotificationRepository;
import co.ufps.studymatch.student.StudentId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GetDashboardQuery {
    private final GetStudentHandler getStudentHandler;
    private final GetStudentGroupsQuery getStudentGroupsQuery;
    private final RecommendationService recommendationService;
    private final INotificationRepository notificationRepository;
    private final IStudyGroupRepository groupRepository;

    @Inject
    public GetDashboardQuery(GetStudentHandler getStudentHandler,
                             GetStudentGroupsQuery getStudentGroupsQuery,
                             RecommendationService recommendationService,
                             INotificationRepository notificationRepository,
                             IStudyGroupRepository groupRepository) {
        this.getStudentHandler = getStudentHandler;
        this.getStudentGroupsQuery = getStudentGroupsQuery;
        this.recommendationService = recommendationService;
        this.notificationRepository = notificationRepository;
        this.groupRepository = groupRepository;
    }

    public DashboardDTO execute(String studentId) {
        StudentDTO student = getStudentHandler.execute(new GetStudentCommand(studentId));
        StudentId id = StudentId.of(studentId);
        return new DashboardDTO(
                student,
                getStudentGroupsQuery.execute(studentId),
                recommendationService.recommendGroups(studentId, "composite", 3),
                recommendationService.recommendStudents(studentId, "composite", 3),
                notificationRepository.contarNoLeidas(id),
                groupRepository.contarSolicitudesPendientes(id)
        );
    }
}
