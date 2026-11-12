package co.ufps.studymatch.api.meta;

import co.ufps.studymatch.application.recommendation.ListStrategiesQuery;
import co.ufps.studymatch.application.recommendation.StrategyInfoDTO;
import co.ufps.studymatch.application.subject.SearchSubjectsQuery;
import co.ufps.studymatch.application.subject.SubjectDTO;
import co.ufps.studymatch.group.GroupModality;
import co.ufps.studymatch.group.GroupStatus;
import co.ufps.studymatch.group.RequestStatus;
import co.ufps.studymatch.notification.NotificationType;
import co.ufps.studymatch.student.AvailabilityStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;

@Path("/api/v1/meta")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class MetaResource {
    private final SearchSubjectsQuery searchSubjectsQuery;
    private final ListStrategiesQuery listStrategiesQuery;

    @Inject
    public MetaResource(SearchSubjectsQuery searchSubjectsQuery, ListStrategiesQuery listStrategiesQuery) {
        this.searchSubjectsQuery = searchSubjectsQuery;
        this.listStrategiesQuery = listStrategiesQuery;
    }

    @GET
    @Path("/options")
    public MetaOptionsDTO options() {
        List<SubjectDTO> subjects = searchSubjectsQuery.execute(null);
        List<String> areas = subjects.stream()
                .map(SubjectDTO::area)
                .distinct()
                .sorted()
                .toList();
        return new MetaOptionsDTO(
                Arrays.stream(DayOfWeek.values()).map(Enum::name).toList(),
                Arrays.stream(GroupModality.values()).map(Enum::name).toList(),
                Arrays.stream(GroupStatus.values()).map(Enum::name).toList(),
                transitions(),
                Arrays.stream(AvailabilityStatus.values()).map(Enum::name).toList(),
                Arrays.stream(RequestStatus.values()).map(Enum::name).toList(),
                Arrays.stream(NotificationType.values()).map(Enum::name).toList(),
                areas,
                subjects,
                listStrategiesQuery.execute()
        );
    }

    private List<StatusTransitionsDTO> transitions() {
        return Arrays.stream(GroupStatus.values())
                .map(status -> new StatusTransitionsDTO(status.name(),
                        Arrays.stream(GroupStatus.values())
                                .filter(status::canTransitionTo)
                                .map(Enum::name)
                                .toList()))
                .toList();
    }

    public record MetaOptionsDTO(
            List<String> daysOfWeek,
            List<String> modalities,
            List<String> groupStatuses,
            List<StatusTransitionsDTO> groupStatusTransitions,
            List<String> availabilityStatuses,
            List<String> requestStatuses,
            List<String> notificationTypes,
            List<String> subjectAreas,
            List<SubjectDTO> subjects,
            List<StrategyInfoDTO> strategies
    ) {
    }

    public record StatusTransitionsDTO(String status, List<String> canTransitionTo) {
    }
}
