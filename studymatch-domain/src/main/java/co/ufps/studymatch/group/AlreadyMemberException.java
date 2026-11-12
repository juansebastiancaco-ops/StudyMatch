package co.ufps.studymatch.group;

import co.ufps.studymatch.ConflictException;

public class AlreadyMemberException extends ConflictException {
    public AlreadyMemberException() {
        super("El estudiante ya es miembro de este grupo");
    }
}
