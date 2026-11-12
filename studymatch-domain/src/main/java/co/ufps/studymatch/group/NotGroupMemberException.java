package co.ufps.studymatch.group;

import co.ufps.studymatch.ConflictException;

public class NotGroupMemberException extends ConflictException {
    public NotGroupMemberException(String studentId) {
        super("El estudiante no pertenece al grupo: " + studentId);
    }
}
