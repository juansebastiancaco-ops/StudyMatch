package co.ufps.studymatch.group;

import co.ufps.studymatch.ConflictException;

public class DuplicateJoinRequestException extends ConflictException {
    public DuplicateJoinRequestException() {
        super("Ya existe una solicitud pendiente de este estudiante para el grupo");
    }
}
