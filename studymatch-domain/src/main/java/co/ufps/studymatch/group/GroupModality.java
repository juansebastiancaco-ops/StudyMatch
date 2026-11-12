package co.ufps.studymatch.group;

import co.ufps.studymatch.ValidationException;

public enum GroupModality {
    VIRTUAL,
    IN_PERSON,
    HYBRID;

    public static GroupModality fromString(String value) {
        if (value == null || value.isBlank()) {
            return VIRTUAL;
        }
        String normalized = value.strip().toUpperCase().replace("-", "_").replace(" ", "_");
        return switch (normalized) {
            case "VIRTUAL", "ONLINE", "REMOTO" -> VIRTUAL;
            case "IN_PERSON", "PRESENCIAL", "PRESENTIAL" -> IN_PERSON;
            case "HYBRID", "HIBRIDO", "MIXTO" -> HYBRID;
            default -> throw new ValidationException("Modalidad invalida: " + value
                    + ". Valores validos: VIRTUAL, IN_PERSON, HYBRID");
        };
    }
}
