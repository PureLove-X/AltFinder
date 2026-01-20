package tech.purelove.altfinder.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import tech.purelove.altfinder.database.dao.PlayerDao;
import tech.purelove.altfinder.util.log.LogUtils;

import java.sql.SQLException;

public class PlayerQuitListener implements Listener {

    private final PlayerDao playerDao;

    public PlayerQuitListener(PlayerDao playerDao) {
        this.playerDao = playerDao;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        String uuid = event.getPlayer().getUniqueId().toString();
        long now = System.currentTimeMillis() / 1000;

        try {
            playerDao.updateLogout(uuid, now);
        } catch (SQLException e) {
            LogUtils.error(e.getMessage());
        }
    }
}
