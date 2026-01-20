package tech.purelove.altfinder.config;

public record Config(
        // storage
        String sqliteFile,
        // notifications
        boolean notifyEnable,
        String notifyMsg,
        // tracking
        boolean logIps,
        boolean logUsernameChanges,
        int recentNameChangeDays,

        // alt limits
        int concurrentAlts,
        String concurrentKickmsg,

        // commands - seen
        boolean seenShowIpByDefault,
        boolean seenRequirePermissionForIp,

        // commands - search
        int searchResultsPerPage
) {}
