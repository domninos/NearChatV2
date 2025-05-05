package net.omni.nearChat.managers;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.TransactionResult;
import net.omni.nearChat.NearChatPlugin;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {
    private final Map<UUID, Boolean> enabled = new HashMap<>();

    private static final String KEY = "enabled";

    private final NearChatPlugin plugin;

    public PlayerManager(NearChatPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadEnabled(Player player) {
        // TODO from database || on join

        // TODO: database have hashset structure]
        //  * KEY,uuid,owner,value (NearChat 2.0)

        UUID uuid = player.getUniqueId();

        if (!has(uuid)) {
            plugin.getDatabaseHandler().asyncHashGet(KEY, uuid.toString()).thenAcceptAsync((string) -> {
                enabled.put(uuid, Boolean.valueOf(string));
                plugin.sendConsole("[DEBUG] Added " + player.getName() + " | " + Boolean.valueOf(string));
            });
        } else {
            plugin.getDatabaseHandler().asyncHashSet(KEY, uuid.toString(), "false");
            enabled.put(uuid, false);
            plugin.sendConsole("[DEBUG] Added " + player.getName() + " | false");
        }
    }

    public void saveToDatabase(Player player) {
        if (has(player.getUniqueId())) {
            Boolean val = enabled.get(player.getUniqueId());

            plugin.getDatabaseHandler().asyncHashSet(KEY, player.getUniqueId().toString(), val.toString());
        }
    }

    public void saveToDatabase() {
        RedisFuture<String> multi = plugin.getDatabaseHandler().asyncMulti();

        enabled.forEach(((uuid, value) ->
                plugin.getDatabaseHandler().asyncHashSet(KEY, uuid.toString(), value.toString())));

        RedisFuture<TransactionResult> exec = plugin.getDatabaseHandler().getAsyncExec();

        exec.whenComplete((result, throwable) -> {
            if (throwable != null) {
                plugin.error(throwable.getMessage());
                return;
            }

            if (!result.isEmpty())
                result.forEach(o -> plugin.sendConsole("[DEBUG] " + o.toString()));

            plugin.sendConsole("&aSaved database.");
        });
    }

    public void toggle(UUID uuid) {
        if (has(uuid))
            this.enabled.replace(uuid, !isEnabled(uuid));
    }

    public boolean isEnabled(UUID uuid) {
        return enabled.getOrDefault(uuid, false);
    }

    public boolean has(UUID uuid) {
        return enabled.containsKey(uuid);
    }

    public void flush() {
        enabled.clear();
    }
}
