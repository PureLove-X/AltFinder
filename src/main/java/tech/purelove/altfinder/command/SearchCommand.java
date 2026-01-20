package tech.purelove.altfinder.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import tech.purelove.altfinder.config.Config;
import tech.purelove.altfinder.database.dao.IpLogDao;
import tech.purelove.altfinder.database.dao.PlayerDao;
import tech.purelove.altfinder.database.model.PlayerRecord;
import tech.purelove.altfinder.util.log.LogUtils;

import java.sql.SQLException;
import java.util.List;

public class SearchCommand {

    private final PlayerDao playerDao;
    private final IpLogDao ipLogDao;
    private final Config config;

    public SearchCommand(PlayerDao playerDao, IpLogDao ipLogDao, Config config) {
        this.playerDao = playerDao;
        this.ipLogDao = ipLogDao;
        this.config = config;
    }

    public void execute(CommandSender sender, String[] args) {

        if (!sender.hasPermission("altfinder.search")) {
            sender.sendMessage(
                    Component.text("You do not have permission to use this command.", NamedTextColor.RED)
            );
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(
                    Component.text("Usage: /altfinder search <player | ip> [page]", NamedTextColor.RED)
            );
            return;
        }

        int page = 1;
        if (args.length >= 2) {
            try {
                page = Math.max(1, Integer.parseInt(args[1]));
            } catch (NumberFormatException ignored) {}
        }

        try {
            String ip = resolveIp(args[0]);
            if (ip == null) {
                sender.sendMessage(
                        Component.text("Player or IP not found.", NamedTextColor.RED)
                );
                return;
            }

            int perPage = config.searchResultsPerPage();
            int total = ipLogDao.countByIp(ip);
            int maxPage = Math.max(1, (int) Math.ceil(total / (double) perPage));

            if (page > maxPage) {
                sender.sendMessage(
                        Component.text("Page out of range. Max page: " + maxPage, NamedTextColor.RED)
                );
                return;
            }

            int offset = (page - 1) * perPage;
            List<String> results = ipLogDao.searchByIp(ip, perPage, offset);

            sender.sendMessage(
                    Component.text("AltFinder Search", NamedTextColor.GOLD)
            );

            sender.sendMessage(
                    Component.text("IP: ", NamedTextColor.YELLOW)
                            .append(Component.text(ip, NamedTextColor.WHITE))
            );

            sender.sendMessage(
                    Component.text("Page " + page + " / " + maxPage, NamedTextColor.GRAY)
            );

            for (int i = 0; i < results.size(); i++) {
                sender.sendMessage(
                        Component.text(offset + i + 1 + ") ", NamedTextColor.YELLOW)
                                .append(Component.text(results.get(i), NamedTextColor.WHITE))
                );
            }

            if (results.isEmpty()) {
                sender.sendMessage(
                        Component.text("No results.", NamedTextColor.GRAY)
                );
            }

        } catch (SQLException e) {
            sender.sendMessage(
                    Component.text("An error occurred while searching.", NamedTextColor.RED)
            );
            LogUtils.error(e.getMessage());
        }
    }

    private String resolveIp(String input) throws SQLException {
        if (input.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
            return input;
        }

        PlayerRecord player = playerDao.findByNameOrUuid(input);
        if (player == null) return null;

        return ipLogDao.getLastIp(player.uuid());
    }
}
