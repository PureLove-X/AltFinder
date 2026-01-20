package tech.purelove.altfinder.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.util.Map;

public final class TextProcessor {

    private static final LegacyComponentSerializer COLOR =
            LegacyComponentSerializer.legacyAmpersand();

    private TextProcessor() {}

    public static Component process(String message, Player player) {
        if (message == null) {
            return Component.empty();
        }

        String processed = message
                .replace("%player_name%", player.getName())
                .replace("%player_uuid%", player.getUniqueId().toString());

        return COLOR.deserialize(processed);
    }

    public static Component process(String message, Map<String, String> placeholders) {
        if (message == null) {
            return Component.empty();
        }

        String processed = message;

        for (var entry : placeholders.entrySet()) {
            processed = processed.replace(entry.getKey(), entry.getValue());
        }

        return COLOR.deserialize(processed);
    }
}
