package co.ufps.studymatch.student;

import co.ufps.studymatch.ConflictException;

public class MaxActiveGroupsExceededException extends ConflictException {
    public MaxActiveGroupsExceededException(int max) {
        super("Un estudiante puede pertenecer como maximo a " + max + " grupos activos");
    }
}
