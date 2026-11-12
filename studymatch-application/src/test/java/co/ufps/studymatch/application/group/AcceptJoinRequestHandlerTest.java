package co.ufps.studymatch.application.group;

import co.ufps.studymatch.application.action.CommandInvoker;
import co.ufps.studymatch.group.GroupModality;
import co.ufps.studymatch.group.GroupStatus;
import co.ufps.studymatch.group.IStudyGroupRepository;
import co.ufps.studymatch.group.JoinRequest;
import co.ufps.studymatch.group.RequestStatus;
import co.ufps.studymatch.group.StudyGroup;
import co.ufps.studymatch.shared.DomainEventPublisher;
import co.ufps.studymatch.shared.events.JoinRequestAcceptedEvent;
import co.ufps.studymatch.student.AvailabilitySlot;
import co.ufps.studymatch.student.AvailabilityStatus;
import co.ufps.studymatch.student.IStudentRepository;
import co.ufps.studymatch.student.Student;
import co.ufps.studymatch.subject.SubjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcceptJoinRequestHandlerTest {
    @Mock
    IStudyGroupRepository groupRepository;
    @Mock
    IStudentRepository studentRepository;
    @Mock
    DomainEventPublisher eventPublisher;
    @Mock
    CommandInvoker commandInvoker;
    @Mock
    GroupDtoAssembler groupAssembler;

    private AcceptJoinRequestHandler handler;
    private Student leader;
    private Student applicant;
    private StudyGroup group;
    private JoinRequest request;

    @BeforeEach
    void setUp() {
        handler = new AcceptJoinRequestHandler(groupRepository, studentRepository, eventPublisher,
                commandInvoker, groupAssembler);
        leader = Student.register("lider-accept@ufps.edu.co", "$2a$10$hash", "Líder",
                "Ingeniería de Sistemas", 5, null, List.of(), AvailabilityStatus.AVAILABLE,
                List.of(AvailabilitySlot.create("MONDAY", "14:00", "16:00")));
        applicant = Student.register("aspirante-accept@ufps.edu.co", "$2a$10$hash", "Aspirante",
                "Ingeniería Industrial", 4, null, List.of(), AvailabilityStatus.AVAILABLE,
                List.of(AvailabilitySlot.create("MONDAY", "14:00", "16:00")));
        group = StudyGroup.create(SubjectId.generate(), "Grupo de física", "Descripción",
                leader.getStudentId(), 4,
                List.of(AvailabilitySlot.create("MONDAY", "14:00", "16:00")), GroupModality.VIRTUAL);
        group.changeStatus(leader.getStudentId(), GroupStatus.OPEN);
        request = group.sendJoinRequest(applicant.getStudentId(), "Quiero unirme", true);
    }

    @Test
    void aceptaSolicitudAgregaMiembroYPublicaEvento() {
        when(groupRepository.buscarId(group.getGroupId())).thenReturn(Optional.of(group));
        when(studentRepository.buscarId(applicant.getStudentId())).thenReturn(Optional.of(applicant));
        when(groupAssembler.toRequestDto(any(JoinRequest.class)))
                .thenAnswer(invocation -> JoinRequestDTO.from(invocation.getArgument(0), applicant));

        JoinRequestDTO dto = handler.execute(new AcceptJoinRequestCommand(
                leader.getStudentId().value(), group.getGroupId().value(), request.getRequestId().value()));

        assertEquals(RequestStatus.ACCEPTED.name(), dto.status());
        assertTrue(group.isMember(applicant.getStudentId()));
        assertEquals(2, group.getMemberCount());
        verify(groupRepository).guardar(group);
        verify(eventPublisher).publish(any(JoinRequestAcceptedEvent.class));
        verify(commandInvoker).registrar(any());
    }
}
