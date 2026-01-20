package tech.purelove.altfinder.database.dao;

import tech.purelove.altfinder.database.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AcknowledgedLinkDao {

    private final Database database;

    public AcknowledgedLinkDao(Database database) {
        this.database = database;
    }

    /* -----------------------------
     * Public API
     * ----------------------------- */

    public void acknowledge(
            String uuid1,
            String uuid2,
            String reason,
            String acknowledgedBy,
            long now
    ) throws SQLException {

        UuidPair pair = normalize(uuid1, uuid2);

        try (PreparedStatement stmt = database.getConnection().prepareStatement(
                """
                INSERT OR REPLACE INTO acknowledged_links
                (uuid_a, uuid_b, reason, acknowledged_by, acknowledged_at)
                VALUES (?, ?, ?, ?, ?)
                """
        )) {
            stmt.setString(1, pair.a());
            stmt.setString(2, pair.b());
            stmt.setString(3, reason);
            stmt.setString(4, acknowledgedBy);
            stmt.setLong(5, now);
            stmt.executeUpdate();
        }
    }

    public void unacknowledge(String uuid1, String uuid2) throws SQLException {
        UuidPair pair = normalize(uuid1, uuid2);

        try (PreparedStatement stmt = database.getConnection().prepareStatement(
                """
                DELETE FROM acknowledged_links
                WHERE uuid_a = ? AND uuid_b = ?
                """
        )) {
            stmt.setString(1, pair.a());
            stmt.setString(2, pair.b());
            stmt.executeUpdate();
        }
    }

    public boolean isAcknowledged(String uuid1, String uuid2) throws SQLException {
        UuidPair pair = normalize(uuid1, uuid2);

        try (PreparedStatement stmt = database.getConnection().prepareStatement(
                """
                SELECT 1
                FROM acknowledged_links
                WHERE uuid_a = ? AND uuid_b = ?
                LIMIT 1
                """
        )) {
            stmt.setString(1, pair.a());
            stmt.setString(2, pair.b());

            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    /* -----------------------------
     * Helpers
     * ----------------------------- */

    private UuidPair normalize(String u1, String u2) {
        if (u1.compareTo(u2) <= 0) {
            return new UuidPair(u1, u2);
        }
        return new UuidPair(u2, u1);
    }

    private record UuidPair(String a, String b) {}


}
