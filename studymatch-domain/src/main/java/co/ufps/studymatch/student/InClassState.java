package co.ufps.studymatch.student;

public final class InClassState implements AvailabilityState {
    @Override
    public AvailabilityStatus status() {
        return AvailabilityStatus.IN_CLASS;
    }

    @Override
    public AvailabilityState transitionTo(AvailabilityStatus target) {
        if (target == null) {
            throw new InvalidAvailabilityTransitionException("La disponibilidad destino es obligatoria");
        }
        return switch (target) {
            case IN_CLASS -> this;
            case AVAILABLE -> new AvailableState();
            case OFFLINE -> new OfflineState();
            case BUSY -> throw invalid(target);
        };
    }

    private InvalidAvailabilityTransitionException invalid(AvailabilityStatus target) {
        return new InvalidAvailabilityTransitionException("No se puede pasar de IN_CLASS a " + target
                + ". Primero debes volver a AVAILABLE u OFFLINE");
    }
}
