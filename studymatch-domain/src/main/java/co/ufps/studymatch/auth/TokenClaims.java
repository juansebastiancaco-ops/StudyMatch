package co.ufps.studymatch.auth;

import co.ufps.studymatch.student.StudentId;

public record TokenClaims(StudentId studentId, String email, String fullName, long expiresAtEpochSeconds) {
}
