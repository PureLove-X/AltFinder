package tech.purelove.altfinder;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import tech.purelove.altfinder.config.ConfigManager;
import tech.purelove.altfinder.database.Database;
import tech.purelove.altfinder.database.dao.PlayerDao;
import tech.purelove.altfinder.util.bootstrap.CommandBootstrap;
import tech.purelove.altfinder.util.bootstrap.ListenerBootstrap;
import tech.purelove.altfinder.util.log.LogUtils;

import java.sql.SQLException;

public final class AltFinder extends JavaPlugin {

    private ConfigManager configManager;
    private Database database;
    private PlayerDao playerDao;

    @Override
    public void onEnable() {
        LogUtils.init(this);
        configManager = new ConfigManager(this);
        configManager.load();

        database = new Database(this, configManager.get());
        database.init();
        playerDao = new PlayerDao(database);
        new ListenerBootstrap(this, database, configManager.get()).register();
        new CommandBootstrap(this, database, configManager).register();

    }

    @Override
    public void onDisable() {
        long now = System.currentTimeMillis();
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                playerDao.updateLogout(player.getUniqueId().toString(), now);
            } catch (SQLException e) {
                LogUtils.error(e.getMessage());
            }
        }
        if (database != null) {
            database.close();
        }
    }
}
