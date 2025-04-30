package net.omni.nearChat.handlers;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.util.NearChatConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class MessageHandler {
    private final NearChatPlugin plugin;
    private final NearChatConfig messageConfig;

    private final Map<String, String> childToMessage = new HashMap<>();

    public MessageHandler(NearChatPlugin plugin) {
        this.plugin = plugin;
        this.messageConfig = plugin.getMessageConfig();
    }

    public void load() {
        childToMessage.clear();

        ConfigurationSection messageSection = getConfig().getConfigurationSection("messages");

        if (messageSection == null) {
            plugin.sendConsole("`messages` not found. Creating..");

            messageSection = getConfig().createSection("messages");
            loadDefaults(messageSection);
        }

        for (String key : messageSection.getKeys(true)) {
            if (key.isBlank()) continue;

            String msg = messageSection.getString(key);

            childToMessage.put(key, msg);
            plugin.sendConsole("&9LoadKeys");

        }

        plugin.sendConsole("&aLoaded messages");
    }

    public void saveToConfig() {
        // TODO: save childToMessage to messages.yml
    }

    public String getPlayersOnly() {
        return childToMessage.getOrDefault("player_only", "&c`player_only` could not be found");
    }

    private void loadDefaults(ConfigurationSection... sections) {
        /*
            messages:
              player_only: "&cOnly players can execute this command."
         */

        for (ConfigurationSection s : sections) {
            if (s.getName().equalsIgnoreCase("messages")) {
                // set
                s.set("player_only", "&cOnly players can execute this command.");
                // TODO
                plugin.sendConsole("&9LoadDefaults");
            }
        }

        this.messageConfig.save();
    }

    public FileConfiguration getConfig() {
        return this.messageConfig.getConfig();
    }

    public void flush() {
        childToMessage.clear();
    }
}
