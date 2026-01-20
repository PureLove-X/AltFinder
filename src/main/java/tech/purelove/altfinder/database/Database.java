package tech.purelove.altfinder.database;

import tech.purelove.altfinder.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import tech.purelove.altfinder.util.log.LogUtils;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private final JavaPlugin plugin;
    private final Config config;
    private Connection connection;

    public Database(JavaPlugin plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void init() {
        try {
            openConnection();
            createTables();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize database");
            LogUtils.error(e.getMessage());
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    private void openConnection() throws SQLException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new SQLException("Could not create plugin data folder");
        }

        File dbFile = new File(dataFolder, config.sqliteFile());
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        connection = DriverManager.getConnection(url);
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS players (
                    uuid TEXT PRIMARY KEY,
                    current_name TEXT NOT NULL,
                    first_seen INTEGER NOT NULL,
                    last_login INTEGER NOT NULL,
                    last_logout INTEGER
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS name_history (
                    uuid TEXT NOT NULL,
                    username TEXT NOT NULL,
                    first_seen INTEGER NOT NULL,
                    last_seen INTEGER NOT NULL,
                    PRIMARY KEY (uuid, username)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS ip_log (
                            uuid TEXT NOT NULL,
                            ip TEXT NOT NULL,
                            first_seen INTEGER NOT NULL,
                            last_seen INTEGER NOT NULL,
                            PRIMARY KEY (uuid, ip)
                        );
            """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS acknowledged_links (
                        uuid_a TEXT NOT NULL,
                        uuid_b TEXT NOT NULL,
                        reason TEXT,
                        acknowledged_by TEXT NOT NULL,
                        acknowledged_at INTEGER NOT NULL,
                        PRIMARY KEY (uuid_a, uuid_b)
                    );
                    """);

            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_ip_log_ip ON ip_log(ip);
            """);

            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_name_history_uuid ON name_history(uuid);
            """);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {}
    }


}
