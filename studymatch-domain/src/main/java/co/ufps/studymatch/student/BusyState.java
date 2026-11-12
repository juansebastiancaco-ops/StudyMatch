package co.ufps.studymatch.student;

public final class BusyState implements AvailabilityState {
    @Override
    public AvailabilityStatus status() {
        return AvailabilityStatus.BUSY;
    }

    @Override
    public AvailabilityState transitionTo(AvailabilityStatus target) {
        if (target == null) {
            throw new InvalidAvailabilityTransitionException("La disponibilidad destino es obligatoria");
        }
        return switch (target) {
            case BUSY -> this;
            case AVAILABLE -> new AvailableState();
            case OFFLINE -> new OfflineState();
            case IN_CLASS -> throw invalid(target);
        };
    }

    private InvalidAvailabilityTransitionException invalid(AvailabilityStatus target) {
        return new InvalidAvailabilityTransitionException("No se puede pasar de BUSY a " + target
                + ". Primero debes volver a AVAILABLE u OFFLINE");
    }
}
