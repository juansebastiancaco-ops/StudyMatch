package co.ufps.studymatch.group;

import co.ufps.studymatch.ConflictException;

public enum RequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    CANCELLED;

    public boolean canTransitionTo(RequestStatus target) {
        if (target == null || target == this) {
            return false;
        }
        return switch (this) {
            case PENDING -> target == ACCEPTED || target == REJECTED || target == CANCELLED;
            case ACCEPTED, REJECTED, CANCELLED -> false;
        };
    }

    public RequestStatus transitionTo(RequestStatus target) {
        if (!canTransitionTo(target)) {
            throw new InvalidRequestStateException("Transicion de solicitud invalida: " + this + " -> " + target);
        }
        return target;
    }

    public boolean isResolved() {
        return this != PENDING;
    }
}
