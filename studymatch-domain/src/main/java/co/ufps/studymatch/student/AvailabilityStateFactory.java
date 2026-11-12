package co.ufps.studymatch.student;

public final class AvailabilityStateFactory {
    private AvailabilityStateFactory() {
    }

    public static AvailabilityState from(AvailabilityStatus status) {
        if (status == null) {
            return new AvailableState();
        }
        return switch (status) {
            case AVAILABLE -> new AvailableState();
            case BUSY -> new BusyState();
            case IN_CLASS -> new InClassState();
            case OFFLINE -> new OfflineState();
        };
    }
}
