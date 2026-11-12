package co.ufps.studymatch.group;

import co.ufps.studymatch.ConflictException;

public class ScheduleConflictException extends ConflictException {
    public ScheduleConflictException() {
        super("El horario del grupo no coincide con la disponibilidad del estudiante");
    }
}
