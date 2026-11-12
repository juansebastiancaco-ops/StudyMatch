package co.ufps.studymatch.student;

/**
 * Patron State: la disponibilidad delega sus transiciones en el estado concreto.
 */
public interface AvailabilityState {
    AvailabilityStatus status();

    AvailabilityState transitionTo(AvailabilityStatus target);

    default boolean canTransitionTo(AvailabilityStatus target) {
        try {
            transitionTo(target);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
