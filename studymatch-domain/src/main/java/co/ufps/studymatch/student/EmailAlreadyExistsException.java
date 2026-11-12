package co.ufps.studymatch.student;

import co.ufps.studymatch.ConflictException;

public class EmailAlreadyExistsException extends ConflictException {
    public EmailAlreadyExistsException(String email) {
        super("Ya existe un estudiante con ese email: " + email);
    }
}
