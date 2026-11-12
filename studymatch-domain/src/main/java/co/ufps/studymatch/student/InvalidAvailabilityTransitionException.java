package co.ufps.studymatch.student;

import co.ufps.studymatch.ConflictException;

public class InvalidAvailabilityTransitionException extends ConflictException {
    public InvalidAvailabilityTransitionException(String message) {
        super(message);
    }
}
