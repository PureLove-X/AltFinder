package tech.purelove.altfinder.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import tech.purelove.altfinder.database.dao.AcknowledgedLinkDao;
import tech.purelove.altfinder.database.dao.PlayerDao;
import tech.purelove.altfinder.util.log.LogUtils;

import java.sql.SQLException;

public class UnacknowledgeCommand {

    private final PlayerDao playerDao;
    private final AcknowledgedLinkDao ackDao;

    public UnacknowledgeCommand(PlayerDao playerDao, AcknowledgedLinkDao ackDao) {
        this.playerDao = playerDao;
        this.ackDao = ackDao;
    }

    public void execute(CommandSender sender, String[] args) {

        if (!sender.hasPermission("altfinder.unacknowledge")) {
            sender.sendMessage(
                    Component.text("You do not have permission to do that.", NamedTextColor.RED)
            );
            return;
        }

        if (args.length != 2) {
            sender.sendMessage(
                    Component.text(
                            "Usage: /altfinder unacknowledge <player1> <player2>",
                            NamedTextColor.RED
                    )
            );
            return;
        }

        try {
            String uuid1 = playerDao.getUuidByName(args[0]);
            String uuid2 = playerDao.getUuidByName(args[1]);

            if (uuid1 == null || uuid2 == null) {
                sender.sendMessage(
                        Component.text("One or more players could not be found.", NamedTextColor.RED)
                );
                return;
            }

            ackDao.unacknowledge(uuid1, uuid2);

            sender.sendMessage(
                    Component.text("Relationship removed.", NamedTextColor.GREEN)
            );

        } catch (SQLException e) {
            sender.sendMessage(
                    Component.text("A database error occurred.", NamedTextColor.RED)
            );
            LogUtils.error(e.getMessage());
        }
    }
}
