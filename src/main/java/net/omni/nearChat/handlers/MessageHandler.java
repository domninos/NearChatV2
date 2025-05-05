package net.omni.nearChat.handlers;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.util.NearChatConfig;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageHandler {
    private final NearChatPlugin plugin;
    private final NearChatConfig messageConfig;
    private final Map<String, String> childToMessage = new HashMap<>();
    private final Map<String, List<String>> childToListMessage = new HashMap<>();

    public MessageHandler(NearChatPlugin plugin) {
        this.plugin = plugin;
        this.messageConfig = plugin.getMessageConfig();
    }

    public void load() { // TODO ADD MORE MESSAGES
        childToMessage.clear();

        boolean def = loadDefaults(); // check if there are empty messages, if so, replace it

        // load messages to cache
        childToMessage.put("player_only", getConfig().getString("player_only"));
        childToMessage.put("no_permission", getConfig().getString("no_permission"));
        childToMessage.put("db_connected", getConfig().getString("db_connected"));

        childToListMessage.put("help_text", getConfig().getStringList("help_text"));

        if (!def)
            plugin.sendConsole("&aLoaded messages");
    }

    public void saveToConfig() {
        for (Map.Entry<String, String> k : childToMessage.entrySet()) {
            String key = k.getKey();
            String value = k.getValue();

            messageConfig.setNoSave(key, value);
        }

        messageConfig.save();
    }

    public String getPlayerOnly() {
        return childToMessage.getOrDefault("player_only", "&c`player_only`");
    }

    public String getNoPermission() {
        return childToMessage.getOrDefault("no_permission", "&c`no_permission`");
    }

    public String getDBConnected() {
        return childToMessage.getOrDefault("db_connected", "&c`db_connected`");
    }

    public List<String> getHelpTextList() {
        return childToListMessage.getOrDefault("help_text", List.of("null"));
    }

    private boolean loadDefaults() {
        boolean def = false;

        if (getConfig().getString("player_only") == null) {
            messageConfig.setNoSave("player_only", "&cOnly players can execute this command.");
            def = true;
        }

        if (getConfig().getString("no_permission") == null) {
            messageConfig.setNoSave("no_permission", "&cYou do not have permission to use this command.");
            def = true;
        }

        if (getConfig().getString("db_connected") == null) {
            messageConfig.setNoSave("db_connected", "&aSuccessfully connected to database!");
            def = true;
        }

        if (getConfig().getStringList("help_text").isEmpty()) {
            messageConfig.setNoSave("help_text", Arrays.asList(
                    "&7-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-",
                    "&a/nearchat >> Opens the NearChat GUI.",
                    "&a/nearchat help >> Opens this menu.",
                    "<nearchat.db> &a/nearchat database >> Opens the NearChat GUI.",
                    "&a/nearchat database >> Opens the NearChat GUI. ",
                    "",
                    "&bDISCORD: discord.gg/nearchat",
                    "&7-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-"
            ));

            def = true;
        }

        if (def) {
            plugin.sendConsole("&9Loaded default messages.");
            this.messageConfig.save();
        }

        return def;
    }

    public FileConfiguration getConfig() {
        return this.messageConfig.getConfig();
    }

    public void flush() {
        childToMessage.clear();
    }
}
