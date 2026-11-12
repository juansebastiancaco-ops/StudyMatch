package co.ufps.studymatch.group;

import co.ufps.studymatch.NotFoundException;

public class GroupNotFoundException extends NotFoundException {
    public GroupNotFoundException(String groupId) {
        super("Grupo de estudio no encontrado con id: " + groupId);
    }
}
