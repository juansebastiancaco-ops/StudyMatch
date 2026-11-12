package co.ufps.studymatch.application.group;

import co.ufps.studymatch.application.action.CommandInvoker;
import co.ufps.studymatch.group.GroupModality;
import co.ufps.studymatch.group.GroupStatus;
import co.ufps.studymatch.group.IStudyGroupRepository;
import co.ufps.studymatch.group.JoinRequest;
import co.ufps.studymatch.group.JoinRequestId;
import co.ufps.studymatch.group.RequestStatus;
import co.ufps.studymatch.group.StudyGroup;
import co.ufps.studymatch.group.StudyGroupId;
import co.ufps.studymatch.shared.DomainEventPublisher;
import co.ufps.studymatch.shared.events.JoinRequestSentEvent;
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
class SendJoinRequestHandlerTest {
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

    private SendJoinRequestHandler handler;
    private Student leader;
    private Student applicant;
    private StudyGroup group;

    @BeforeEach
    void setUp() {
        handler = new SendJoinRequestHandler(groupRepository, studentRepository, eventPublisher,
                commandInvoker, groupAssembler);
        leader = Student.register("lider@ufps.edu.co", "$2a$10$hash", "Líder",
                "Ingeniería de Sistemas", 5, null, List.of(), AvailabilityStatus.AVAILABLE,
                List.of(AvailabilitySlot.create("MONDAY", "14:00", "16:00")));
        applicant = Student.register("aspirante@ufps.edu.co", "$2a$10$hash", "Aspirante",
                "Ingeniería Industrial", 4, null, List.of(), AvailabilityStatus.AVAILABLE,
                List.of(AvailabilitySlot.create("MONDAY", "14:00", "16:00")));
        group = StudyGroup.create(SubjectId.generate(), "Grupo de cálculo", "Descripción",
                leader.getStudentId(), 4,
                List.of(AvailabilitySlot.create("MONDAY", "14:00", "16:00")), GroupModality.HYBRID);
        group.changeStatus(leader.getStudentId(), GroupStatus.OPEN);
    }

    @Test
    void enviaSolicitudPersisteEventoYDevuelveDto() {
        when(groupRepository.buscarId(group.getGroupId())).thenReturn(Optional.of(group));
        when(studentRepository.buscarId(applicant.getStudentId())).thenReturn(Optional.of(applicant));
        when(groupAssembler.toRequestDto(any(JoinRequest.class)))
                .thenAnswer(invocation -> JoinRequestDTO.from(invocation.getArgument(0), applicant));

        JoinRequestDTO dto = handler.execute(new SendJoinRequestCommand(
                applicant.getStudentId().value(), group.getGroupId().value(), "Quiero unirme"));

        assertEquals(RequestStatus.PENDING.name(), dto.status());
        assertTrue(group.hasPendingRequestFrom(applicant.getStudentId()));
        verify(groupRepository).guardar(group);
        verify(eventPublisher).publish(any(JoinRequestSentEvent.class));
        verify(commandInvoker).registrar(any());
    }
}
