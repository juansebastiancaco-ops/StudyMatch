package co.ufps.studymatch.application.auth;

import co.ufps.studymatch.application.student.StudentDTO;
import co.ufps.studymatch.application.student.StudentDtoAssembler;
import co.ufps.studymatch.auth.IssuedToken;
import co.ufps.studymatch.auth.PasswordHasher;
import co.ufps.studymatch.auth.TokenIssuer;
import co.ufps.studymatch.shared.DomainEvent;
import co.ufps.studymatch.shared.DomainEventPublisher;
import co.ufps.studymatch.student.EmailAlreadyExistsException;
import co.ufps.studymatch.student.IStudentRepository;
import co.ufps.studymatch.student.Student;
import co.ufps.studymatch.subject.ISubjectRepository;
import co.ufps.studymatch.subject.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterStudentHandlerTest {
    @Mock
    IStudentRepository studentRepository;
    @Mock
    ISubjectRepository subjectRepository;
    @Mock
    PasswordHasher passwordHasher;
    @Mock
    DomainEventPublisher eventPublisher;
    @Mock
    TokenIssuer tokenIssuer;
    @Mock
    StudentDtoAssembler studentAssembler;

    private RegisterStudentHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RegisterStudentHandler(studentRepository, subjectRepository, passwordHasher,
                eventPublisher, tokenIssuer, studentAssembler);
    }

    @Test
    void registraEstudiantePublicaEventoYDevuelveToken() {
        Subject subject = Subject.create("SIS301", "Estructuras de Datos", "Software", 4, 4);
        StudentDTO dto = new StudentDTO("student-id", "nuevo@ufps.edu.co", "Nuevo Estudiante",
                "Ingeniería de Sistemas", 4, "ACTIVE", LocalDateTime.now(), null, null);
        RegisterStudentCommand command = new RegisterStudentCommand(
                "nuevo@ufps.edu.co", "Password123", "Nuevo Estudiante", "Ingeniería de Sistemas",
                4, "Bio", List.of(subject.getSubjectId().value()), "AVAILABLE", List.of());

        when(studentRepository.existeEmail(command.email())).thenReturn(false);
        when(subjectRepository.buscarPorIds(any())).thenReturn(List.of(subject));
        when(passwordHasher.hash("Password123")).thenReturn("$2a$10$hash");
        when(studentRepository.guardar(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenIssuer.issue(any(), anyString(), anyString()))
                .thenReturn(new IssuedToken("jwt-token", Instant.now().plusSeconds(3600)));
        when(studentAssembler.toDto(any(Student.class))).thenReturn(dto);

        AuthResponse response = handler.execute(command);

        assertEquals("jwt-token", response.token());
        assertEquals("Bearer", response.tokenType());
        assertEquals(dto, response.student());
        assertNotNull(response.expiresAt());
        verify(studentRepository).guardar(any(Student.class));
        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertEquals("WELCOME", eventCaptor.getValue().notificationType().name());
    }

    @Test
    void rechazaEmailDuplicadoSinPersistir() {
        RegisterStudentCommand command = new RegisterStudentCommand(
                "existente@ufps.edu.co", "Password123", "Estudiante", "Ingeniería", 3,
                null, List.of(), "AVAILABLE", List.of());
        when(studentRepository.existeEmail(command.email())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> handler.execute(command));

        verify(studentRepository, never()).guardar(any(Student.class));
        verify(eventPublisher, never()).publish(any());
    }
}
