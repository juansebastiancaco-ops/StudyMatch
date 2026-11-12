package co.ufps.studymatch.student;

public final class OfflineState implements AvailabilityState {
    @Override
    public AvailabilityStatus status() {
        return AvailabilityStatus.OFFLINE;
    }

    @Override
    public AvailabilityState transitionTo(AvailabilityStatus target) {
        if (target == null) {
            throw new InvalidAvailabilityTransitionException("La disponibilidad destino es obligatoria");
        }
        return switch (target) {
            case OFFLINE -> this;
            case AVAILABLE -> new AvailableState();
            case BUSY, IN_CLASS -> throw invalid(target);
        };
    }

    private InvalidAvailabilityTransitionException invalid(AvailabilityStatus target) {
        return new InvalidAvailabilityTransitionException("No se puede pasar de OFFLINE a " + target
                + ". Primero debes volver a AVAILABLE");
    }
}
