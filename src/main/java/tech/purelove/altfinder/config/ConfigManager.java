package tech.purelove.altfinder.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {

    private final JavaPlugin plugin;
    private Config config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = readConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = readConfig();
    }

    private Config readConfig() {
        FileConfiguration c = plugin.getConfig();

        return new Config(
                // storage
                c.getString("storage.sqlite.file", "altfinder.db"),
                // notifications
                c.getBoolean("notifications.enabled", true),
                c.getString("notifications.message", ""),
                // tracking
                c.getBoolean("tracking.log-ip-addresses", true),
                c.getBoolean("tracking.log-username-changes", true),
                c.getInt("tracking.recent-name-change-days", 30),

                // alt limits
                c.getInt("limits.concurrent-alts.max", -1),
                c.getString("limits.concurrent-alts.kick-message", ""),

                // commands - seen
                c.getBoolean("commands.seen.show-ip-by-default", false),
                c.getBoolean("commands.seen.require-permission-for-ip", true),

                // commands - search
                c.getInt("commands.search.results-per-page", 10)
        );
    }


    public Config get() {
        return config;
    }
}
