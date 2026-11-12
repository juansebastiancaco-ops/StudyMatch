package co.ufps.studymatch.group;

import co.ufps.studymatch.ConflictException;

public class InvalidGroupTransitionException extends ConflictException {
    public InvalidGroupTransitionException(String message) {
        super(message);
    }
}
