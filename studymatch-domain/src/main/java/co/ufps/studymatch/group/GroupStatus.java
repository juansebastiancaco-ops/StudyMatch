package co.ufps.studymatch.group;

import co.ufps.studymatch.ConflictException;

/**
 * Unica fuente de verdad de la maquina de estados de un grupo.
 */
public enum GroupStatus {
    DRAFT,
    OPEN,
    FULL,
    IN_PROGRESS,
    COMPLETED,
    ARCHIVED;

    public boolean canTransitionTo(GroupStatus target) {
        if (target == null || target == this) {
            return false;
        }
        return switch (this) {
            case DRAFT -> target == OPEN || target == ARCHIVED;
            case OPEN -> target == FULL || target == IN_PROGRESS || target == ARCHIVED;
            case FULL -> target == OPEN || target == IN_PROGRESS || target == ARCHIVED;
            case IN_PROGRESS -> target == COMPLETED || target == ARCHIVED;
            case COMPLETED -> target == ARCHIVED;
            case ARCHIVED -> false;
        };
    }

    public GroupStatus transitionTo(GroupStatus target) {
        if (!canTransitionTo(target)) {
            throw new InvalidGroupTransitionException("Transicion de grupo invalida: " + this + " -> " + target);
        }
        return target;
    }

    public boolean isPublic() {
        return this == OPEN || this == FULL;
    }

    public boolean isActive() {
        return this == OPEN || this == FULL || this == IN_PROGRESS;
    }

    public boolean acceptsRequests() {
        return this == OPEN;
    }
}
