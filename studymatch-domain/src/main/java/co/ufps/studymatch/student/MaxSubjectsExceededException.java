package co.ufps.studymatch.student;

import co.ufps.studymatch.ValidationException;

public class MaxSubjectsExceededException extends ValidationException {
    public MaxSubjectsExceededException(String message) {
        super(message);
    }
}
