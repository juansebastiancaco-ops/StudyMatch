package co.ufps.studymatch.application.auth;

import co.ufps.studymatch.application.common.UnauthorizedException;
import co.ufps.studymatch.application.student.StudentDtoAssembler;
import co.ufps.studymatch.auth.PasswordHasher;
import co.ufps.studymatch.auth.TokenIssuer;
import co.ufps.studymatch.student.IStudentRepository;
import co.ufps.studymatch.student.Student;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class LoginHandler {
    private final IStudentRepository studentRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;
    private final StudentDtoAssembler studentAssembler;

    @Inject
    public LoginHandler(IStudentRepository studentRepository,
                        PasswordHasher passwordHasher,
                        TokenIssuer tokenIssuer,
                        StudentDtoAssembler studentAssembler) {
        this.studentRepository = studentRepository;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
        this.studentAssembler = studentAssembler;
    }

    public AuthResponse execute(LoginCommand command) {
        Student student = studentRepository.buscarPorEmail(command.email().strip().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Credenciales invalidas"));
        if (!passwordHasher.verify(command.password(), student.getPasswordHash())) {
            throw new UnauthorizedException("Credenciales invalidas");
        }
        if (!student.isActive()) {
            throw new UnauthorizedException("La cuenta esta suspendida. Contacta al administrador.");
        }
        var token = tokenIssuer.issue(student.getStudentId(), student.getEmail(), student.getFullName());
        return new AuthResponse(token.token(), "Bearer", token.expiresAt(), studentAssembler.toDto(student));
    }
}
