package co.ufps.studymatch.action;

import co.ufps.studymatch.NotFoundException;

public class ActionHistoryNotFoundException extends NotFoundException {
    public ActionHistoryNotFoundException(String actionId) {
        super("Accion no encontrada con id: " + actionId);
    }
}
