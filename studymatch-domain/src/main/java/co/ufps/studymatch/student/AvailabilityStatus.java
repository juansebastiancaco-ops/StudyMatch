package co.ufps.studymatch.student;

import co.ufps.studymatch.ValidationException;

public enum AvailabilityStatus {
    AVAILABLE,
    BUSY,
    IN_CLASS,
    OFFLINE;

    public static AvailabilityStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return AVAILABLE;
        }
        String normalized = value.strip().toUpperCase()
                .replace("-", "_")
                .replace(" ", "_");
        return switch (normalized) {
            case "AVAILABLE", "DISPONIBLE" -> AVAILABLE;
            case "BUSY", "OCUPADO" -> BUSY;
            case "IN_CLASS", "EN_CLASE", "CLASE" -> IN_CLASS;
            case "OFFLINE", "DESCONECTADO", "FUERA_DE_LINEA" -> OFFLINE;
            default -> throw new ValidationException("Disponibilidad invalida: " + value
                    + ". Valores validos: AVAILABLE, BUSY, IN_CLASS, OFFLINE");
        };
    }
}
