package tech.purelove.altfinder.database.dao;

import tech.purelove.altfinder.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NameHistoryDao {

    private final Database database;

    public NameHistoryDao(Database database) {
        this.database = database;
    }

    public void record(String uuid, String username, long now) throws SQLException {
        Connection conn = database.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(
                """
                INSERT INTO name_history (uuid, username, first_seen, last_seen)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(uuid, username)
                DO UPDATE SET last_seen = excluded.last_seen
                """
        )) {
            stmt.setString(1, uuid);
            stmt.setString(2, username);
            stmt.setLong(3, now);
            stmt.setLong(4, now);
            stmt.executeUpdate();
        }
    }

    public String getPreviousUsername(String uuid, String current) throws SQLException {
        Connection conn = database.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(
                """
                SELECT username
                FROM name_history
                WHERE uuid = ?
                  AND LOWER(username) != LOWER(?)
                ORDER BY last_seen DESC
                LIMIT 1
                """
        )) {
            stmt.setString(1, uuid);
            stmt.setString(2, current);

            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("username") : null;
        }
    }
}
