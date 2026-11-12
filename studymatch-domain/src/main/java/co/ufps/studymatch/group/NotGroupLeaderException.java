package co.ufps.studymatch.group;

import co.ufps.studymatch.ForbiddenDomainException;

public class NotGroupLeaderException extends ForbiddenDomainException {
    public NotGroupLeaderException() {
        super("Solo el lider del grupo puede realizar esta accion");
    }
}
