package co.ufps.studymatch.student;

public final class AvailableState implements AvailabilityState {
    @Override
    public AvailabilityStatus status() {
        return AvailabilityStatus.AVAILABLE;
    }

    @Override
    public AvailabilityState transitionTo(AvailabilityStatus target) {
        if (target == null) {
            throw new InvalidAvailabilityTransitionException("La disponibilidad destino es obligatoria");
        }
        return switch (target) {
            case AVAILABLE -> this;
            case BUSY -> new BusyState();
            case IN_CLASS -> new InClassState();
            case OFFLINE -> new OfflineState();
        };
    }
}
