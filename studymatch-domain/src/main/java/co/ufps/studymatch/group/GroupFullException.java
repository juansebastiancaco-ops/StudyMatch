package co.ufps.studymatch.group;

import co.ufps.studymatch.ConflictException;

public class GroupFullException extends ConflictException {
    public GroupFullException(String groupId) {
        super("El grupo ya alcanzo su capacidad maxima: " + groupId);
    }
}
