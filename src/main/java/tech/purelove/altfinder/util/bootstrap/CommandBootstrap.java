package tech.purelove.altfinder.util.bootstrap;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import tech.purelove.altfinder.command.*;
import tech.purelove.altfinder.config.ConfigManager;
import tech.purelove.altfinder.database.Database;
import tech.purelove.altfinder.database.dao.AcknowledgedLinkDao;
import tech.purelove.altfinder.database.dao.IpLogDao;
import tech.purelove.altfinder.database.dao.NameHistoryDao;
import tech.purelove.altfinder.database.dao.PlayerDao;

public class CommandBootstrap {

    private final JavaPlugin plugin;
    private final Database database;
    private final ConfigManager configManager;

    public CommandBootstrap(JavaPlugin plugin, Database database, ConfigManager configManager) {
        this.plugin = plugin;
        this.database = database;
        this.configManager = configManager;
    }

    public void register() {
        PlayerDao playerDao = new PlayerDao(database);
        NameHistoryDao nameHistoryDao = new NameHistoryDao(database);
        IpLogDao ipLogDao = new IpLogDao(database);
        AcknowledgedLinkDao ackDao = new AcknowledgedLinkDao(database);

        // /seen (standalone)
        PluginCommand seen = plugin.getCommand("seen");
        if (seen != null) {
            SeenCommand seenCommand =
                    new SeenCommand(playerDao, nameHistoryDao, ipLogDao, configManager.get());

            seen.setExecutor(seenCommand);
            seen.setTabCompleter(seenCommand);
        }

        // /altfinder (dispatcher)
        SearchCommand search = new SearchCommand(playerDao, ipLogDao, configManager.get());
        DeleteCommand delete = new DeleteCommand(playerDao, ipLogDao, configManager.get());
        AcknowledgeCommand acknowledgeCommand = new AcknowledgeCommand(playerDao, ackDao);
        UnacknowledgeCommand unacknowledgeCommand = new UnacknowledgeCommand(playerDao, ackDao);
        UnresolvedCommand unresolved = new UnresolvedCommand(playerDao, ipLogDao);
        ResolvedCommand resolved = new ResolvedCommand(playerDao, ipLogDao);
        AltFinderCommand dispatcher =
                new AltFinderCommand(search, delete, acknowledgeCommand, unacknowledgeCommand, unresolved, resolved);

        PluginCommand altfinder = plugin.getCommand("altfinder");
        if (altfinder != null) {
            altfinder.setExecutor(dispatcher);
            altfinder.setTabCompleter(dispatcher);
        }
    }
}
