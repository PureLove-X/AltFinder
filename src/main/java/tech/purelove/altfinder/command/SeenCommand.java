package tech.purelove.altfinder.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jspecify.annotations.NonNull;
import tech.purelove.altfinder.config.Config;
import tech.purelove.altfinder.database.dao.IpLogDao;
import tech.purelove.altfinder.database.dao.NameHistoryDao;
import tech.purelove.altfinder.database.dao.PlayerDao;
import tech.purelove.altfinder.database.model.PlayerRecord;
import tech.purelove.altfinder.util.RelativeTime;
import tech.purelove.altfinder.util.log.LogUtils;

import java.sql.SQLException;
import java.util.List;

public class SeenCommand implements CommandExecutor, TabCompleter {

    private final PlayerDao playerDao;
    private final NameHistoryDao nameHistoryDao;
    private final IpLogDao ipLogDao;
    private final Config config;

    public SeenCommand(
            PlayerDao playerDao,
            NameHistoryDao nameHistoryDao,
            IpLogDao ipLogDao,
            Config config
    ) {
        this.playerDao = playerDao;
        this.nameHistoryDao = nameHistoryDao;
        this.ipLogDao = ipLogDao;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {

        if (!sender.hasPermission("altfinder.seen")) {
            sender.sendMessage(
                    Component.text("You do not have permission to use this command.", NamedTextColor.RED)
            );
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(
                    Component.text("Usage: /seen <player>", NamedTextColor.RED)
            );
            return true;
        }

        try {
            PlayerRecord player = playerDao.findForSeen(args[0]);
            if (player == null) {
                sender.sendMessage(
                        Component.text("Player not found.", NamedTextColor.RED)
                );
                return true;
            }

            sender.sendMessage(
                    Component.text("Seen information", NamedTextColor.GOLD)
            );

            sender.sendMessage(
                    Component.text("Player: ", NamedTextColor.YELLOW)
                            .append(Component.text(player.username(), NamedTextColor.WHITE))
            );

            boolean showIp =
                    config.seenShowIpByDefault()
                            || (config.seenRequirePermissionForIp()
                            && sender.hasPermission("altfinder.seen.ip"));

            if (showIp) {
                List<String> ips = ipLogDao.getRecentIps(player.uuid(), 3);
                if (!ips.isEmpty()) {
                    sender.sendMessage(
                            Component.text("Recent IPs: ", NamedTextColor.YELLOW)
                                    .append(Component.text(String.join(", ", ips), NamedTextColor.WHITE))
                    );
                }
            }

            Component lastSeen;

            if (player.lastLogout() == null) {
                lastSeen = Component.text("Online now", NamedTextColor.GREEN);
            } else {
                lastSeen = Component.text(
                        RelativeTime.format(player.lastLogout()),
                        NamedTextColor.WHITE
                );
            }

            sender.sendMessage(
                    Component.text("Last seen: ", NamedTextColor.YELLOW)
                            .append(lastSeen)
            );


            String previous = nameHistoryDao.getPreviousUsername(
                    player.uuid(),
                    player.username()
            );

            if (previous != null) {
                sender.sendMessage(
                        Component.text("Previous username: ", NamedTextColor.YELLOW)
                                .append(Component.text(previous, NamedTextColor.WHITE))
                );
            }
            if (sender.hasPermission("altfinder.seen.alt")) {
            List<String> alts = ipLogDao.findPossibleAlts(player.uuid());
            if (!alts.isEmpty()) {

                sender.sendMessage(
                        Component.text("Possible alt accounts: ", NamedTextColor.YELLOW)
                                .append(Component.text(
                                        String.join(", ", alts) + " (" + alts.size() + ")",
                                        NamedTextColor.WHITE
                                ))
                );
            }
            }

        } catch (SQLException e) {
            sender.sendMessage(
                    Component.text("An error occurred while looking up that player.", NamedTextColor.RED)
            );
            LogUtils.error(e.getMessage());
        }

        return true;
    }
    @Override
    public List<String> onTabComplete(
            @NonNull CommandSender sender,
            @NonNull Command command,
            @NonNull String alias,
            String @NonNull [] args
    ) {
        // Intentionally return no suggestions
        return List.of();
    }

}
