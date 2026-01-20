package tech.purelove.altfinder.database.model;

public record PlayerRecord(
        String uuid,
        String username,
        long lastLogin,
        Long lastLogout
) {}
