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

public class DeleteCommand {

    private final PlayerDao playerDao;
    private final IpLogDao ipLogDao;
    private final Config config;

    public DeleteCommand(PlayerDao playerDao, IpLogDao ipLogDao, Config config) {
        this.playerDao = playerDao;
        this.ipLogDao = ipLogDao;
        this.config = config;
    }

    public void execute(CommandSender sender, String[] args) {

        if (!sender.hasPermission("altfinder.delete")) {
            sender.sendMessage(
                    Component.text("You do not have permission to do that.", NamedTextColor.RED)
            );
            return;
        }

        if (args.length != 1) {
            sender.sendMessage(
                    Component.text("Usage: /altfinder delete <player|ip>", NamedTextColor.RED)
            );
            return;
        }

        try {
            String input = args[0];

            // If input is an IP
            if (input.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
                int removed = ipLogDao.deleteByIp(input);
                sender.sendMessage(
                        Component.text(
                                "Removed " + removed + " IP log entr" + (removed == 1 ? "y." : "ies."),
                                NamedTextColor.GREEN
                        )
                );
                return;
            }

            // Otherwise resolve player
            PlayerRecord player = playerDao.findByNameOrUuid(input);
            if (player == null) {
                sender.sendMessage(
                        Component.text("Player not found.", NamedTextColor.RED)
                );
                return;
            }

            int removed = ipLogDao.deleteByUuid(player.uuid());
            sender.sendMessage(
                    Component.text(
                            "Removed " + removed + " IP log entr" + (removed == 1 ? "y" : "ies")
                                    + " for " + player.username() + ".",
                            NamedTextColor.GREEN
                    )
            );

        } catch (SQLException e) {
            sender.sendMessage(
                    Component.text("An error occurred while deleting data.", NamedTextColor.RED)
            );
            LogUtils.error(e.getMessage());
        }
    }
}
