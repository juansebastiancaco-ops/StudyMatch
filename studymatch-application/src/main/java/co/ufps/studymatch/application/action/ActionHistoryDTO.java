package co.ufps.studymatch.application.action;

import co.ufps.studymatch.action.ActionHistoryEntry;

import java.time.LocalDateTime;

public record ActionHistoryDTO(
        String actionId,
        String type,
        String description,
        LocalDateTime createdAt,
        boolean undone,
        boolean canUndo
) {
    public static ActionHistoryDTO from(ActionHistoryEntry entry, boolean canUndo) {
        return new ActionHistoryDTO(
                entry.getActionId(),
                entry.getType(),
                entry.getDescription(),
                entry.getCreatedAt(),
                entry.isUndone(),
                canUndo && !entry.isUndone()
        );
    }
}
