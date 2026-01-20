package tech.purelove.altfinder.util.bootstrap;

import tech.purelove.altfinder.AltFinder;
import tech.purelove.altfinder.config.Config;
import tech.purelove.altfinder.database.Database;
import tech.purelove.altfinder.database.dao.AcknowledgedLinkDao;
import tech.purelove.altfinder.database.dao.IpLogDao;
import tech.purelove.altfinder.database.dao.NameHistoryDao;
import tech.purelove.altfinder.database.dao.PlayerDao;
import tech.purelove.altfinder.listener.PlayerJoinListener;
import tech.purelove.altfinder.listener.PlayerQuitListener;
import tech.purelove.altfinder.listener.VanishListener;
import tech.purelove.altfinder.util.log.LogUtils;

public class ListenerBootstrap {

    private final AltFinder plugin;
    private final Database database;
    private final Config config;

    public ListenerBootstrap(AltFinder plugin, Database database, Config config) {
        this.plugin = plugin;
        this.database = database;
        this.config = config;
    }

    public void register() {
        PlayerDao playerDao = new PlayerDao(database);
        NameHistoryDao nameHistoryDao = new NameHistoryDao(database);
        IpLogDao ipLogDao = new IpLogDao(database);
        AcknowledgedLinkDao ackDao = new AcknowledgedLinkDao(database);

        plugin.getServer().getPluginManager().registerEvents(
                new PlayerJoinListener(
                        playerDao,
                        nameHistoryDao,
                        ipLogDao,
                        ackDao,
                        config
                ),
                plugin
        );
        plugin.getServer().getPluginManager().registerEvents(
                new PlayerQuitListener(playerDao),
                plugin
        );

        var pm = plugin.getServer().getPluginManager();

        if (pm.isPluginEnabled("SuperVanish") || pm.isPluginEnabled("PremiumVanish")) {
            pm.registerEvents(new VanishListener(playerDao), plugin);
            LogUtils.info("Vanish hook enabled");
        }

    }
}