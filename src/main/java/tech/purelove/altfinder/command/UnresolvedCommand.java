package tech.purelove.altfinder.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import tech.purelove.altfinder.database.dao.IpLogDao;
import tech.purelove.altfinder.database.dao.PlayerDao;
import tech.purelove.altfinder.database.model.UnresolvedIpGroup;
import tech.purelove.altfinder.database.model.UnresolvedPair;
import tech.purelove.altfinder.util.log.LogUtils;

import java.sql.SQLException;
import java.util.List;

public class UnresolvedCommand {

    private static final int PAGE_SIZE = 5;

    private final PlayerDao playerDao;
    private final IpLogDao ipLogDao;

    public UnresolvedCommand(PlayerDao playerDao, IpLogDao ipLogDao) {
        this.playerDao = playerDao;
        this.ipLogDao = ipLogDao;
    }

    public void execute(CommandSender sender, String[] args) {

        if (!sender.hasPermission("altfinder.unresolved")) {
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
            int totalPairs = ipLogDao.countUnresolvedPairs();
            if (totalPairs == 0) {
                sender.sendMessage(Component.text(
                        "No unresolved alt relationships found.",
                        NamedTextColor.GREEN
                ));
                return;
            }

            int maxPage = (int) Math.ceil(totalPairs / (double) PAGE_SIZE);
            if (page > maxPage) page = maxPage;

            sender.sendMessage(Component.text(
                    "Unresolved alt relationships (Page " + page + "/" + maxPage + ")",
                    NamedTextColor.GOLD
            ));

            List<UnresolvedIpGroup> groups =
                    ipLogDao.findUnresolvedByIp(PAGE_SIZE, offset);

            for (UnresolvedIpGroup group : groups) {
                sender.sendMessage(Component.text(
                        "IP: " + group.ip(),
                        NamedTextColor.YELLOW
                ));

                for (UnresolvedPair pair : group.pairs()) {
                    String nameA = playerDao.findByNameOrUuid(pair.uuidA()).username();
                    String nameB = playerDao.findByNameOrUuid(pair.uuidB()).username();

                    sender.sendMessage(Component.text(
                            "  - " + nameA + " ↔ " + nameB,
                            NamedTextColor.WHITE
                    ));
                }
            }

        } catch (SQLException e) {
            sender.sendMessage(Component.text(
                    "An error occurred while fetching unresolved alts.",
                    NamedTextColor.RED
            ));
            LogUtils.error(e.getMessage());
        }
    }

}

