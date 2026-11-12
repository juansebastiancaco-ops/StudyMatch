package co.ufps.studymatch.group;

import co.ufps.studymatch.ConflictException;

public class InvalidRequestStateException extends ConflictException {
    public InvalidRequestStateException(String message) {
        super(message);
    }
}
