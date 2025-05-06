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
    private final Map<String, String> childToMessage = new HashMap<>();
    private final Map<String, List<String>> childToListMessage = new HashMap<>();
    private NearChatConfig messageConfig;

    public MessageHandler(NearChatPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() { // TODO ADD MORE MESSAGES
        if (this.messageConfig == null)
            this.messageConfig = plugin.getMessageConfig();

        getConfig().options().copyDefaults(true);
        messageConfig.save();

        childToMessage.clear();

        boolean def = loadDefaults(); // check if there are empty messages, if so, replace it

        // load messages to cache
        childToMessage.put("player_only", getConfig().getString("player_only"));
        childToMessage.put("no_permission", getConfig().getString("no_permission"));
        childToMessage.put("db_connected", getConfig().getString("db_connected"));
        childToMessage.put("db_error_connect_disabled", getConfig().getString("db_error_connect_disabled"));
        childToMessage.put("reloaded_config", getConfig().getString("reloaded_config"));

        childToMessage.put("prefix", getConfig().getString("prefix"));
        childToMessage.put("format", getConfig().getString("format"));

        childToListMessage.put("help_text", getConfig().getStringList("help_text"));

        if (!def)
            plugin.sendConsole("&aLoaded messages");
    }

    public void saveToConfig() {
        for (Map.Entry<String, String> entry : childToMessage.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            messageConfig.setNoSave(key, value);
        }

        for (Map.Entry<String, List<String>> entry : childToListMessage.entrySet()) {
            String key = entry.getKey();
            List<String> value = entry.getValue();

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

    public String getPrefix() {
        return childToMessage.getOrDefault("prefix", "&f[&6Near&eChat&f] &7");
    }

    public String getFormat() {
        return childToMessage.getOrDefault("format", "&c`format`");
    }

    public String getDBErrorConnectDisabled() {
        return childToMessage.getOrDefault("db_error_connect_disabled", "&c`db_error_connect_disabled`");
    }

    public String getReloadedConfig() {
        return childToMessage.getOrDefault("reloaded_config", "&c`reloaded_config`");
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
                    "<nearchat.db> &a/nearchat database >> Reconnects to the database.",
                    "<nearchat.reload> &a/nearchat reload >> Reloads config.yml and messages.yml.",
                    "",
                    "&bDISCORD: discord.gg/nearchat",
                    "",
                    "&6%plugin_name% running %plugin_version% for MC %plugin_mc_version%",
                    "&7-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-"
            ));

            def = true;
        }

        if (getConfig().getString("prefix") == null) {
            messageConfig.setNoSave("prefix", "&f[&6Near&eChat&f] &7");
            def = true;
        }

        if (getConfig().getString("format") == null) {
            messageConfig.setNoSave("format", "%prefix% &r%player%&r: %chat%");
            def = true;
        }

        if (getConfig().getString("db_error_connect_disabled") == null) {
            messageConfig.setNoSave("db_error_connect_disabled", "&cCould not save to database because database is disabled.");
            def = true;
        }

        if (getConfig().getString("reloaded_config") == null) {
            messageConfig.setNoSave("reloaded_config", "&aReloaded config and messages.yml");
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

        if (!childToListMessage.isEmpty())
            childToListMessage.forEach((k, v) -> {
                if (v != null && !v.isEmpty()) v.clear();
            });

        childToListMessage.clear();
    }
}
