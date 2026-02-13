package tech.purelove.altfinder.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.List;

public class AltFinderCommand implements CommandExecutor, TabCompleter {

    private final SearchCommand search;
    private final DeleteCommand delete;
    private final AcknowledgeCommand acknowledge;
    private final UnacknowledgeCommand unacknowledge;
    private final UnresolvedCommand unresolved;
    private final ResolvedCommand resolved;
    private static final List<String> SUBCOMMANDS =
            List.of("search", "delete", "acknowledge", "unacknowledge", "unresolved", "resolved");

    public AltFinderCommand(
            SearchCommand search,
            DeleteCommand delete,
            AcknowledgeCommand acknowledge,
            UnacknowledgeCommand unacknowledge,
            UnresolvedCommand unresolved,
            ResolvedCommand resolved
    ) {
        this.search = search;
        this.delete = delete;
        this.acknowledge = acknowledge;
        this.unacknowledge = unacknowledge;
        this.unresolved = unresolved;
        this.resolved = resolved;
    }



    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage("Usage: /altfinder <search|delete|acknowledge|unacknowledge|unresolved|resolved>");
            return true;
        }

        String sub = args[0].toLowerCase();
        String[] shifted = Arrays.copyOfRange(args, 1, args.length);

        switch (sub) {
            case "search" -> search.execute(sender, shifted);
            case "delete" -> delete.execute(sender, shifted);
            case "acknowledge" -> acknowledge.execute(sender, shifted);
            case "unacknowledge" -> unacknowledge.execute(sender, shifted);
            case "unresolved" -> unresolved.execute(sender, shifted);
            case "resolved" -> resolved.execute(sender, shifted);
            default -> sender.sendMessage("Unknown subcommand.");
        }


        return true;
    }
    @Override
    public List<String> onTabComplete(
            @NonNull CommandSender sender,
            @NonNull Command command,
            @NonNull String alias,
            String[] args
    ) {
        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        // /altfinder search <player|ip>  -> NO suggestions
        if (args.length == 2 && args[0].equalsIgnoreCase("search")) {
            return List.of();
        }
        // Optional: page numbers for search
        if (args.length == 3 && args[0].equalsIgnoreCase("search")) {
            return List.of("<page number>");
        }

        return List.of();
    }

}

