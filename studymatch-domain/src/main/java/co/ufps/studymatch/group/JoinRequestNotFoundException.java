package co.ufps.studymatch.group;

import co.ufps.studymatch.NotFoundException;

public class JoinRequestNotFoundException extends NotFoundException {
    public JoinRequestNotFoundException(String requestId) {
        super("Solicitud de ingreso no encontrada con id: " + requestId);
    }
}
