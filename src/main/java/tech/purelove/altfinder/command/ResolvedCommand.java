package tech.purelove.altfinder.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import tech.purelove.altfinder.database.dao.IpLogDao;
import tech.purelove.altfinder.database.dao.PlayerDao;
import tech.purelove.altfinder.database.model.ResolvedPairView;
import tech.purelove.altfinder.util.log.LogUtils;

import java.sql.SQLException;
import java.util.List;

public class ResolvedCommand {

    private static final int PAGE_SIZE = 5;

    private final PlayerDao playerDao;
    private final IpLogDao ipLogDao;

    public ResolvedCommand(PlayerDao playerDao, IpLogDao ipLogDao) {
        this.playerDao = playerDao;
        this.ipLogDao = ipLogDao;
    }

    public void execute(CommandSender sender, String[] args) {

        if (!sender.hasPermission("altfinder.resolved")) {
            sender.sendMessage(Component.text(
                    "You do not have permission to do that.",
                    NamedTextColor.RED
            ));
            return;
        }

        int page = 1;
        if (args.length >= 1) {
            try {
                page = Math.max(1, Integer.parseInt(args[0]));
            } catch (NumberFormatException ignored) {}
        }

        int offset = (page - 1) * PAGE_SIZE;

        try {
            int total = ipLogDao.countResolvedPairs();
            if (total == 0) {
                sender.sendMessage(Component.text(
                        "No resolved alt relationships found.",
                        NamedTextColor.GREEN
                ));
                return;
            }

            int maxPage = (int) Math.ceil(total / (double) PAGE_SIZE);
            if (page > maxPage) page = maxPage;

            sender.sendMessage(Component.text(
                    "Resolved alt relationships (Page " + page + "/" + maxPage + ")",
                    NamedTextColor.GOLD
            ));

            List<ResolvedPairView> results =
                    ipLogDao.findResolvedPairs(PAGE_SIZE, offset);

            for (ResolvedPairView pair : results) {
                var a = playerDao.findByNameOrUuid(pair.uuidA());
                var b = playerDao.findByNameOrUuid(pair.uuidB());
                if (a == null || b == null) continue;

                sender.sendMessage(Component.text(
                        a.username() + " ↔ " + b.username(),
                        NamedTextColor.WHITE
                ));

                sender.sendMessage(Component.text(
                        "  Shared IPs: " + String.join(", ", pair.sharedIps()),
                        NamedTextColor.GRAY
                ));

                if (pair.reason() != null && !pair.reason().isBlank()) {
                    sender.sendMessage(Component.text(
                            "  Reason: " + pair.reason(),
                            NamedTextColor.GRAY
                    ));
                }
            }

        } catch (SQLException e) {
            sender.sendMessage(Component.text(
                    "An error occurred while fetching resolved alts.",
                    NamedTextColor.RED
            ));
            LogUtils.error(e.getMessage());
        }
    }
}

