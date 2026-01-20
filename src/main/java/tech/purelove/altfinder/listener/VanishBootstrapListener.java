package tech.purelove.altfinder.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import tech.purelove.altfinder.database.dao.PlayerDao;

public class VanishBootstrapListener implements Listener {

    private final JavaPlugin plugin;
    private final PlayerDao playerDao;
    private boolean registered = false;

    public VanishBootstrapListener(JavaPlugin plugin, PlayerDao playerDao) {
        this.plugin = plugin;
        this.playerDao = playerDao;
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        String name = event.getPlugin().getName();

        if (!registered && (name.equals("SuperVanish") || name.equals("PremiumVanish"))) {
            plugin.getServer().getPluginManager()
                    .registerEvents(new VanishListener(playerDao), plugin);

            registered = true;
        }
    }
}
