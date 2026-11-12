package co.ufps.studymatch.application.auth;

import co.ufps.studymatch.application.common.UnauthorizedException;
import co.ufps.studymatch.application.student.StudentDTO;
import co.ufps.studymatch.application.student.StudentDtoAssembler;
import co.ufps.studymatch.auth.IssuedToken;
import co.ufps.studymatch.auth.PasswordHasher;
import co.ufps.studymatch.auth.TokenIssuer;
import co.ufps.studymatch.student.AvailabilityStatus;
import co.ufps.studymatch.student.IStudentRepository;
import co.ufps.studymatch.student.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginHandlerTest {
    @Mock
    IStudentRepository studentRepository;
    @Mock
    PasswordHasher passwordHasher;
    @Mock
    TokenIssuer tokenIssuer;
    @Mock
    StudentDtoAssembler studentAssembler;

    private LoginHandler handler;

    @BeforeEach
    void setUp() {
        handler = new LoginHandler(studentRepository, passwordHasher, tokenIssuer, studentAssembler);
    }

    private Student student() {
        return Student.register("laura@ufps.edu.co", "$2a$10$hash", "Laura Gómez",
                "Ingeniería de Sistemas", 5, null, List.of(), AvailabilityStatus.AVAILABLE, List.of());
    }

    @Test
    void autenticaCredencialesValidasYDevuelveToken() {
        Student student = student();
        StudentDTO dto = new StudentDTO(student.getStudentId().value(), student.getEmail(),
                student.getFullName(), student.getCareer(), student.getSemester(), "ACTIVE",
                LocalDateTime.now(), null, null);
        when(studentRepository.buscarPorEmail("laura@ufps.edu.co")).thenReturn(Optional.of(student));
        when(passwordHasher.verify("Password123", "$2a$10$hash")).thenReturn(true);
        when(tokenIssuer.issue(any(), anyString(), anyString()))
                .thenReturn(new IssuedToken("token", Instant.now().plusSeconds(3600)));
        when(studentAssembler.toDto(student)).thenReturn(dto);

        AuthResponse response = handler.execute(new LoginCommand(" LAURA@ufps.edu.co ", "Password123"));

        assertEquals("token", response.token());
        assertEquals(dto, response.student());
    }

    @Test
    void rechazaPasswordIncorrecta() {
        Student student = student();
        when(studentRepository.buscarPorEmail("laura@ufps.edu.co")).thenReturn(Optional.of(student));
        when(passwordHasher.verify("incorrecta", "$2a$10$hash")).thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> handler.execute(new LoginCommand("laura@ufps.edu.co", "incorrecta")));

        verify(tokenIssuer, never()).issue(any(), anyString(), anyString());
    }

    @Test
    void rechazaCuentaSuspendida() {
        Student student = student();
        student.suspend();
        when(studentRepository.buscarPorEmail("laura@ufps.edu.co")).thenReturn(Optional.of(student));
        when(passwordHasher.verify("Password123", "$2a$10$hash")).thenReturn(true);

        assertThrows(UnauthorizedException.class,
                () -> handler.execute(new LoginCommand("laura@ufps.edu.co", "Password123")));
    }

    @Test
    void rechazaEmailInexistente() {
        when(studentRepository.buscarPorEmail("nadie@ufps.edu.co")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class,
                () -> handler.execute(new LoginCommand("nadie@ufps.edu.co", "Password123")));
    }
}
