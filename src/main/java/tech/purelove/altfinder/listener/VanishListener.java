package tech.purelove.altfinder.listener;

import de.myzelyam.api.vanish.PlayerVanishStateChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import tech.purelove.altfinder.database.dao.PlayerDao;
import tech.purelove.altfinder.util.log.LogUtils;

import java.time.Instant;

public class VanishListener implements Listener {

    private final PlayerDao playerDao;

    public VanishListener(PlayerDao playerDao) {
        this.playerDao = playerDao;
    }

    @EventHandler
    public void onVanishStateChange(PlayerVanishStateChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        long now = Instant.now().getEpochSecond();
        String uuid = event.getUUID().toString();

        try {
            if (event.isVanishing()) {
                // vanish = logout
                playerDao.updateLogout(uuid, now);
            } else {
                // unvanish = login (name is required by your DAO)
                playerDao.updateLogin(uuid, event.getName(), now);
            }
        } catch (Exception e) {
            LogUtils.error(e.getMessage());
        }
    }
}