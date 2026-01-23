package tech.purelove.altfinder.database.dao;

import tech.purelove.altfinder.database.Database;
import tech.purelove.altfinder.database.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IpLogDao {

    private final Database database;

    public IpLogDao(Database database) {
        this.database = database;
    }

    public void record(String uuid, String ip, long now) throws SQLException {
        Connection conn = database.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(
                """
                INSERT INTO ip_log (uuid, ip, first_seen, last_seen)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(uuid, ip)
                DO UPDATE SET last_seen = excluded.last_seen
                """
        )) {
            stmt.setString(1, uuid);
            stmt.setString(2, ip);
            stmt.setLong(3, now);
            stmt.setLong(4, now);
            stmt.executeUpdate();
        }
    }
    public String getLastIp(String uuid) throws SQLException {
        Connection conn = database.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(
                """
                SELECT ip
                FROM ip_log
                WHERE uuid = ?
                ORDER BY last_seen DESC
                LIMIT 1
                """
        )) {
            stmt.setString(1, uuid);

            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("ip") : null;
        }
    }
    public List<String> findPossibleAlts(String uuid) throws SQLException {
        try (PreparedStatement stmt = database.getConnection().prepareStatement(
                """
                SELECT DISTINCT p.current_name
                FROM ip_log i
                JOIN ip_log i2 ON i.ip = i2.ip
                JOIN players p ON p.uuid = i2.uuid
                WHERE i.uuid = ?
                  AND i2.uuid != ?
                ORDER BY p.current_name COLLATE NOCASE
                """
        )) {
            stmt.setString(1, uuid);
            stmt.setString(2, uuid);

            ResultSet rs = stmt.executeQuery();
            List<String> results = new ArrayList<>();

            while (rs.next()) {
                results.add(rs.getString("current_name"));
            }

            return results;
        }
    }


    public int countByIp(String ip) throws SQLException {
        Connection conn = database.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(DISTINCT uuid) FROM ip_log WHERE ip = ?"
        )) {
            stmt.setString(1, ip);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    public List<String> searchByIp(String ip, int limit, int offset) throws SQLException {
        try (PreparedStatement stmt = database.getConnection().prepareStatement(
                """
                SELECT p.current_name
                FROM ip_log i
                JOIN players p ON p.uuid = i.uuid
                WHERE i.ip = ?
                ORDER BY i.last_seen DESC, p.current_name COLLATE NOCASE
                LIMIT ? OFFSET ?
                """
        )) {
            stmt.setString(1, ip);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);

            ResultSet rs = stmt.executeQuery();
            List<String> results = new ArrayList<>();

            while (rs.next()) {
                results.add(rs.getString("current_name"));
            }

            return results;
        }
    }

    public int deleteByIp(String ip) throws SQLException {
        Connection conn = database.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM ip_log WHERE ip = ?"
        )) {
            stmt.setString(1, ip);
            return stmt.executeUpdate();
        }
    }
    public int deleteByUuid(String uuid) throws SQLException {
        Connection conn = database.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM ip_log WHERE uuid = ?"
        )) {
            stmt.setString(1, uuid);
            return stmt.executeUpdate();
        }
    }

    public List<String> getRecentIps(String uuid, int limit) throws SQLException {
        try (PreparedStatement stmt = database.getConnection().prepareStatement(
                """
                SELECT ip
                FROM ip_log
                WHERE uuid = ?
                ORDER BY last_seen DESC
                LIMIT ?
                """
        )) {
            stmt.setString(1, uuid);
            stmt.setInt(2, limit);

            ResultSet rs = stmt.executeQuery();
            List<String> ips = new ArrayList<>();

            while (rs.next()) {
                ips.add(rs.getString("ip"));
            }

            return ips;
        }
    }

    public int countUnresolvedPairs() throws SQLException {
        try (var stmt = database.getConnection().prepareStatement(
                """
                SELECT COUNT(DISTINCT i.uuid || ':' || i2.uuid)
                FROM ip_log i
                JOIN ip_log i2 ON i.ip = i2.ip
                WHERE i.uuid < i2.uuid
                  AND NOT EXISTS (
                      SELECT 1
                      FROM acknowledged_links a
                      WHERE a.uuid_a = i.uuid
                        AND a.uuid_b = i2.uuid
                  )
                """
        )) {
            var rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    public List<UnresolvedIpGroup> findUnresolvedByIp(int limit, int offset) throws SQLException {
        try (var stmt = database.getConnection().prepareStatement(
                """
                SELECT DISTINCT
                    i.ip,
                    i.uuid AS uuid_a,
                    i2.uuid AS uuid_b
                FROM ip_log i
                JOIN ip_log i2 ON i.ip = i2.ip
                WHERE i.uuid < i2.uuid
                  AND NOT EXISTS (
                      SELECT 1
                      FROM acknowledged_links a
                      WHERE a.uuid_a = i.uuid
                        AND a.uuid_b = i2.uuid
                  )
                ORDER BY i.ip, i.uuid, i2.uuid
                LIMIT ? OFFSET ?
                """
        )) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);

            var rs = stmt.executeQuery();

            Map<String, List<UnresolvedPair>> grouped = new LinkedHashMap<>();

            while (rs.next()) {
                grouped
                        .computeIfAbsent(rs.getString("ip"), k -> new ArrayList<>())
                        .add(new UnresolvedPair(
                                rs.getString("uuid_a"),
                                rs.getString("uuid_b")
                        ));
            }

            List<UnresolvedIpGroup> results = new ArrayList<>();
            grouped.forEach((ip, pairs) ->
                    results.add(new UnresolvedIpGroup(ip, pairs))
            );

            return results;
        }
    }
    public List<ResolvedPairView> findResolvedPairs(int limit, int offset) throws SQLException {
        try (var stmt = database.getConnection().prepareStatement(
                """
                SELECT
                    a.uuid_a,
                    a.uuid_b,
                    a.reason,
                    GROUP_CONCAT(DISTINCT i.ip) AS shared_ips
                FROM acknowledged_links a
                JOIN ip_log i  ON i.uuid  = a.uuid_a
                JOIN ip_log i2 ON i2.uuid = a.uuid_b
                WHERE i.ip = i2.ip
                GROUP BY a.uuid_a, a.uuid_b
                ORDER BY a.uuid_a, a.uuid_b
                LIMIT ? OFFSET ?
                """
        )) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);

            var rs = stmt.executeQuery();
            List<ResolvedPairView> results = new ArrayList<>();

            while (rs.next()) {
                String ipList = rs.getString("shared_ips");

                results.add(new ResolvedPairView(
                        rs.getString("uuid_a"),
                        rs.getString("uuid_b"),
                        rs.getString("reason"),
                        ipList == null
                                ? List.of()
                                : List.of(ipList.split(","))
                ));
            }

            return results;
        }
    }

    public int countResolvedPairs() throws SQLException {
        try (var stmt = database.getConnection().prepareStatement(
                "SELECT COUNT(*) FROM acknowledged_links"
        )) {
            var rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

}