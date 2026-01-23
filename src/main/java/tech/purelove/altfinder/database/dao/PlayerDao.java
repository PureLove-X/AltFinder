package tech.purelove.altfinder.database.dao;

import tech.purelove.altfinder.database.Database;
import tech.purelove.altfinder.database.model.PlayerRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerDao {

    private final Database database;

    public PlayerDao(Database database) {
        this.database = database;
    }

    /* =======================
       LOGIN / LOGOUT
       ======================= */

    public void updateLogin(String uuid, String username, long now) throws SQLException {
        Connection conn = database.getConnection();

        try (PreparedStatement check = conn.prepareStatement(
                "SELECT 1 FROM players WHERE uuid = ?"
        )) {
            check.setString(1, uuid);
            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                try (PreparedStatement update = conn.prepareStatement(
                        """
                        UPDATE players
                        SET current_name = ?, last_login = ?, last_logout = NULL
                        WHERE uuid = ?
                        """
                )) {
                    update.setString(1, username);
                    update.setLong(2, now);
                    update.setString(3, uuid);
                    update.executeUpdate();
                }
            } else {
                try (PreparedStatement insert = conn.prepareStatement(
                        """
                        INSERT INTO players (uuid, current_name, first_seen, last_login, last_logout)
                        VALUES (?, ?, ?, ?, NULL)
                        """
                )) {
                    insert.setString(1, uuid);
                    insert.setString(2, username);
                    insert.setLong(3, now);
                    insert.setLong(4, now);
                    insert.executeUpdate();
                }
            }
        }
    }

    public void updateLogout(String uuid, long now) throws SQLException {
        try (PreparedStatement stmt = database.getConnection().prepareStatement(
                "UPDATE players SET last_logout = ? WHERE uuid = ?"
        )) {
            stmt.setLong(1, now);
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        }
    }

    /* =======================
       LOOKUPS
       ======================= */

    public PlayerRecord findForSeen(String input) throws SQLException {
        if (isUuid(input)) return findByUuid(input);
        if (isIp(input)) return findByIp(input);
        return findByName(input);
    }

    public PlayerRecord findByNameOrUuid(String input) throws SQLException {
        try (PreparedStatement stmt = database.getConnection().prepareStatement(
                """
                SELECT uuid, current_name, last_login, last_logout
                FROM players
                WHERE uuid = ?
                   OR LOWER(current_name) = LOWER(?)
                """
        )) {
            stmt.setString(1, input);
            stmt.setString(2, input);

            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;

            return mapPlayer(rs);
        }
    }

    public PlayerRecord findByName(String name) throws SQLException {
        try (PreparedStatement stmt = database.getConnection().prepareStatement(
                """
                SELECT uuid, current_name, last_login, last_logout
                FROM players
                WHERE LOWER(current_name) = LOWER(?)
                """
        )) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;

            return mapPlayer(rs);
        }
    }

    public PlayerRecord findByUuid(String uuid) throws SQLException {
        try (PreparedStatement stmt = database.getConnection().prepareStatement(
                """
                SELECT uuid, current_name, last_login, last_logout
                FROM players
                WHERE uuid = ?
                """
        )) {
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;

            return mapPlayer(rs);
        }
    }

    public PlayerRecord findByIp(String ip) throws SQLException {
        try (PreparedStatement stmt = database.getConnection().prepareStatement(
                """
                SELECT p.uuid, p.current_name, p.last_login, p.last_logout
                FROM players p
                JOIN ip_log i ON i.uuid = p.uuid
                WHERE i.ip = ?
                ORDER BY i.last_seen DESC
                LIMIT 1
                """
        )) {
            stmt.setString(1, ip);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;

            return mapPlayer(rs);
        }
    }

    public String getUuidByName(String name) throws SQLException {
        try (PreparedStatement stmt = database.getConnection().prepareStatement(
                "SELECT uuid FROM players WHERE LOWER(current_name) = LOWER(?)"
        )) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("uuid") : null;
        }
    }

    /* =======================
       HELPERS
       ======================= */

    private PlayerRecord mapPlayer(ResultSet rs) throws SQLException {
        long lastLogin = rs.getLong("last_login");

        Long lastLogout = rs.getLong("last_logout");
        if (rs.wasNull()) lastLogout = null;

        return new PlayerRecord(
                rs.getString("uuid"),
                rs.getString("current_name"),
                lastLogin,
                lastLogout
        );
    }

    private boolean isUuid(String input) {
        return input.matches(
                "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
        );
    }

    private boolean isIp(String input) {
        return input.matches("^\\d{1,3}(\\.\\d{1,3}){3}$");
    }
}