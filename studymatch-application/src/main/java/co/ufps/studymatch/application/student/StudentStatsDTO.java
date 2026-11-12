package co.ufps.studymatch.application.student;

public record StudentStatsDTO(
        int activeGroups,
        int ownedGroups,
        long pendingJoinRequests,
        long pendingReceivedRequests,
        long unreadNotifications
) {
}
