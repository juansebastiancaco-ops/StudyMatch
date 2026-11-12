package co.ufps.studymatch.student;

public enum StudentStatus {
    ACTIVE,
    SUSPENDED;

    public boolean canAccessPlatform() {
        return this == ACTIVE;
    }
}
