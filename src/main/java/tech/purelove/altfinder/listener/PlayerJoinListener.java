package tech.purelove.altfinder.listener;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import tech.purelove.altfinder.config.Config;
import tech.purelove.altfinder.database.dao.AcknowledgedLinkDao;
import tech.purelove.altfinder.database.dao.IpLogDao;
import tech.purelove.altfinder.database.dao.NameHistoryDao;
import tech.purelove.altfinder.database.dao.PlayerDao;
import tech.purelove.altfinder.util.TextProcessor;
import tech.purelove.altfinder.util.log.LogUtils;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class PlayerJoinListener implements Listener {

    private final PlayerDao playerDao;
    private final NameHistoryDao nameHistoryDao;
    private final IpLogDao ipLogDao;
    private final Config config;
    private final AcknowledgedLinkDao ackDao;

    public PlayerJoinListener(
            PlayerDao playerDao,
            NameHistoryDao nameHistoryDao,
            IpLogDao ipLogDao,
            AcknowledgedLinkDao ackDao,
            Config config
    ) {
        this.playerDao = playerDao;
        this.nameHistoryDao = nameHistoryDao;
        this.ipLogDao = ipLogDao;
        this.ackDao = ackDao;
        this.config = config;
    }

    /* -----------------------------------------
     * LOGIN — concurrent alt enforcement only
     * ----------------------------------------- */
    @Deprecated
    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        Player joining = event.getPlayer();
        if (joining.hasPermission("altfinder.limit.bypass")) {
            return;
        }
        if (config.concurrentAlts() < 0) return;

        InetAddress address = event.getAddress();
        if (address == null) return; // Paper timing: allow login

        String ip = address.getHostAddress();


        long onlineSameIp = Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getAddress() != null)
                .filter(p -> p.getAddress().getAddress() != null)
                .filter(p -> p.getAddress().getAddress().getHostAddress().equals(ip))
                .filter(p -> !p.getUniqueId().equals(joining.getUniqueId()))
                .count();

        if (onlineSameIp > config.concurrentAlts()) {
            event.disallow(
                    PlayerLoginEvent.Result.KICK_OTHER,
                    TextProcessor.process(config.concurrentKickmsg(), joining)
            );
        }
    }

    /* -----------------------------
     * JOIN — safe DB logging only
     * ----------------------------- */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.getAddress() == null || player.getAddress().getAddress() == null) {
            return;
        }

        String uuid = player.getUniqueId().toString();
        String username = player.getName();
        String ip = player.getAddress().getAddress().getHostAddress();
        long now = System.currentTimeMillis() / 1000;

        try {
            // 1️⃣ Core logging FIRST
            playerDao.updateLogin(uuid, username, now);

            if (config.logUsernameChanges()) {
                nameHistoryDao.record(uuid, username, now);
            }

            if (config.logIps()) {
                ipLogDao.record(uuid, ip, now);
            }

            // 2️⃣ THEN notify staff
            notifyStaffIfAltDetected(player);

        } catch (Exception e) {
            LogUtils.error(e.getMessage());
        }
    }


    /* -----------------------------
     * Staff notification logic
     * ----------------------------- */
    private void notifyStaffIfAltDetected(Player player) {
        if (!config.notifyEnable()) return;

        // Delay notification so it appears after join message
        Bukkit.getScheduler().runTaskLater(
                Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("AltFinder")),
                () -> runAltNotification(player),
                20L // 1 second
        );
    }

        private void runAltNotification(Player player) {
            try {
                List<String> altNames =
                        ipLogDao.findPossibleAlts(player.getUniqueId().toString());

                if (altNames.isEmpty()) return;

                for (String altName : altNames) {
                    String altUuid = playerDao.getUuidByName(altName);
                    if (altUuid == null) continue;

                    // Skip acknowledged relationships
                    if (ackDao.isAcknowledged(player.getUniqueId().toString(), altUuid)) {
                        continue;
                    }

                    Component msg = TextProcessor.process(
                            config.notifyMsg(),
                            player
                    );

                    Bukkit.getOnlinePlayers().stream()
                            .filter(p -> p.hasPermission("altfinder.notify"))
                            .forEach(p -> p.sendMessage(msg));

                    return; // notify once per join
                }

            } catch (SQLException e) {
                LogUtils.error(e.getMessage());
            }
        }

    }
