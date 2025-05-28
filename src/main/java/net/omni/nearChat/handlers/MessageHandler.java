package net.omni.nearChat.handlers;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.database.adapters.DatabaseAdapter;
import net.omni.nearChat.util.MainUtil;
import net.omni.nearChat.util.NearChatConfig;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageHandler {

    private static final List<String> EMPTY_LIST = List.of();

    private final NearChatPlugin plugin;
    private final Map<String, String> childToMessage = new HashMap<>();
    private final Map<String, List<String>> childToListMessage = new HashMap<>();
    private NearChatConfig messageConfig;

    public MessageHandler(NearChatPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        if (this.messageConfig == null)
            this.messageConfig = plugin.getMessageConfig();

        childToMessage.clear();

        boolean def = loadDefaults(); // check if there are empty messages, if so, replace it

        // load messages to cache
        childToMessage.put("player_only", getConfig().getString("player_only"));
        childToMessage.put("no_permission", getConfig().getString("no_permission"));

        childToMessage.put("nearchat_enabled", getConfig().getString("nearchat_enabled"));
        childToMessage.put("nearchat_enabled_player", getConfig().getString("nearchat_enabled_player"));
        childToMessage.put("nearchat_disabled", getConfig().getString("nearchat_disabled"));
        childToMessage.put("nearchat_disabled_player", getConfig().getString("nearchat_disabled_player"));
        childToMessage.put("nearchat_player_not_found", getConfig().getString("nearchat_player_not_found"));

        childToMessage.put("db_init", getConfig().getString("db_init"));
        childToMessage.put("db_connected", getConfig().getString("db_connected"));
        childToMessage.put("db_connected_console", getConfig().getString("db_connected_console"));
        childToMessage.put("db_disconnected", getConfig().getString("db_disconnected"));
        childToMessage.put("db_error_credentials_not_found", getConfig().getString("db_error_credentials_not_found"));
        childToMessage.put("db_error_connect_unsuccessful", getConfig().getString("db_error_connect_unsuccessful"));
        childToMessage.put("db_error_connect_disabled", getConfig().getString("db_error_connect_disabled"));
        childToMessage.put("db_error_connect_already", getConfig().getString("db_error_connect_already"));
        childToMessage.put("db_saved", getConfig().getString("db_saved"));
        childToMessage.put("db_try_save", getConfig().getString("db_try_save"));

        childToMessage.put("reloaded_config", getConfig().getString("reloaded_config"));
        childToMessage.put("created_file", getConfig().getString("created_file"));

        childToMessage.put("prefix", getConfig().getString("prefix"));
        childToMessage.put("format", getConfig().getString("format"));

        childToMessage.put("broker_stop", getConfig().getString("broker_stop"));

        childToListMessage.put("help_text", getConfig().getStringList("help_text"));
        childToListMessage.put("enabled_message", getConfig().getStringList("enabled_message"));
        childToListMessage.put("disabled_message", getConfig().getStringList("disabled_message"));

        if (!def)
            plugin.sendConsole("&aLoaded messages");
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

        if (getConfig().getString("nearchat_enabled") == null) {
            messageConfig.setNoSave("nearchat_enabled", "&aEnabled");
            def = true;
        }

        if (getConfig().getString("nearchat_enabled_player") == null) {
            messageConfig.setNoSave("nearchat_enabled_player", "&aEnabled NearChat for %name%");
            def = true;
        }

        if (getConfig().getString("nearchat_disabled") == null) {
            messageConfig.setNoSave("nearchat_disabled", "&cDisabled");
            def = true;
        }

        if (getConfig().getString("nearchat_disabled_player") == null) {
            messageConfig.setNoSave("nearchat_disabled_player", "&cDisabled NearChat for %name%");
            def = true;
        }

        if (getConfig().getString("nearchat_player_not_found") == null) {
            messageConfig.setNoSave("nearchat_player_not_found", "&cCould not find %name%");
            def = true;
        }

        if (getConfig().getString("db_init") == null) {
            messageConfig.setNoSave("db_init", "&aInitializing &7%db_type%");
            def = true;
        }

        if (getConfig().getString("db_connected") == null) {
            messageConfig.setNoSave("db_connected", "%db_type% &aSuccessfully connected to database!");
            def = true;
        }

        if (getConfig().getString("db_connected_console") == null) {
            messageConfig.setNoSave("db_connected_console", "%db_type% &aSuccessfully connected to: &3%host%");
            def = true;
        }

        if (getConfig().getString("db_disconnected") == null) {
            messageConfig.setNoSave("db_disconnected", "%db_type% &aDatabase disconnected.");
            def = true;
        }

        if (getConfig().getString("db_error_credentials_not_found") == null) {
            messageConfig.setNoSave("db_error_credentials_not_found",
                    "Database information not found in config.yml. Will not use database...");
            def = true;
        }

        if (getConfig().getString("db_error_connect_unsuccessful") == null) {
            messageConfig.setNoSave("db_error_connect_unsuccessful", "%db_type% &cNot successful.");
            def = true;
        }

        if (getConfig().getString("db_error_connect_disabled") == null) {
            messageConfig.setNoSave("db_error_connect_disabled",
                    "%db_type% &cCould not connect to database because database is disabled.");
            def = true;
        }

        if (getConfig().getString("db_error_connect_already") == null) {
            messageConfig.setNoSave("db_error_connect_already",
                    "%db_type% &cCould not connect to database because database is already enabled.");
            def = true;
        }

        if (getConfig().getString("db_saved") == null) {
            messageConfig.setNoSave("db_saved", "[%db_type%] &aSaved database.");
            def = true;
        }

        if (getConfig().getString("db_try_save") == null) {
            messageConfig.setNoSave("db_try_save", "[%db_type%] &cTrying to save database..");
            def = true;
        }

        if (getConfig().getString("reloaded_config") == null) {
            messageConfig.setNoSave("reloaded_config", "&aReloaded config and messages.yml");
            def = true;
        }

        if (getConfig().getString("created_file") == null) {
            messageConfig.setNoSave("created_file", "&aSuccessfully created %file_name%");
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

        if (getConfig().getString("broker_stop") == null) {
            messageConfig.setNoSave("broker_stop", "Database disabled. Cancelling %broker% broker..");
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

        if (getConfig().getStringList("enabled_message").isEmpty()) {
            messageConfig.setNoSave("enabled_message", Arrays.asList(
                    "&aSuccessfully enabled %plugin_name% [%plugin_mc_version%]",
                    "&bSettings:",
                    "  %dDatabase Saving Delay: %db_delay%ms (%db_converted_ticks%)",
                    "  %dNearby Get Delay: %nearby_delay%ms (%nearby_converted_ticks%)",
                    "  %dNearby Radius: %radius% blocks"
            ));

            def = true;
        }

        if (getConfig().getStringList("disabled_message").isEmpty()) {
            messageConfig.setNoSave("disabled_message", List.of(
                    "&aSuccessfully disabled %plugin_name% [%plugin_mc_version%]"
            ));

            def = true;
        }

        if (def) {
            plugin.sendConsole("&9Loaded default messages.");
            this.messageConfig.save();
        }

        return def;
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

    public void sendEnabledMessage() {
        StringBuilder toSend = new StringBuilder();

        for (String line : getEnabledMessage()) {
            if (line.isBlank()) {
                toSend.append("\n");
                continue;
            }

            if (line.contains("%plugin_name%"))
                line = line.replace("%plugin_name%", plugin.getConfigHandler().getPluginName());
            if (line.contains("%plugin_version%"))
                line = line.replace("%plugin_version%", plugin.getConfigHandler().getPluginVersion()); // TODO: get server version
            if (line.contains("%plugin_mc_version%"))
                line = line.replace("%plugin_mc_version%", plugin.getConfigHandler().getPluginMCVersion());
            if (line.contains("%db_type%"))
                line = modifyDBMessage(line);
            if (line.contains("%db_delay%"))
                line = line.replace("%db_delay%", String.valueOf(plugin.getConfigHandler().getDatabaseSaveDelay()));
            if (line.contains("%db_converted_ticks%"))
                line = line.replace("%db_converted_ticks%", MainUtil.convertTicks(plugin.getConfigHandler().getDatabaseSaveDelay()));
            if (line.contains("%nearby_delay%"))
                line = line.replace("%nearby_delay%", String.valueOf(plugin.getConfigHandler().getNearbyGetDelay()));
            if (line.contains("%nearby_converted_ticks%"))
                line = line.replace("%nearby_converted_ticks%", MainUtil.convertTicks(plugin.getConfigHandler().getNearbyGetDelay()));
            if (line.contains("%radius%"))
                line = line.replace("%radius%", String.valueOf(plugin.getConfigHandler().getNearBlockRadius()));

            toSend.append(line).append("&r\n");
        }

        Bukkit.getConsoleSender().sendMessage(plugin.translate(toSend.toString()));
    }

    public void sendDisabledMessage() {
        StringBuilder toSend = new StringBuilder();

        for (String line : getDisabledMessage()) {
            if (line.isBlank()) {
                toSend.append("\n");
                continue;
            }

            if (line.contains("%prefix%"))
                line = line.replace("%prefix%", plugin.getMessageHandler().getPrefix());
            if (line.contains("%plugin_name%"))
                line = line.replace("%plugin_name%", plugin.getConfigHandler().getPluginName());
            if (line.contains("%plugin_mc_version%"))
                line = line.replace("%plugin_mc_version%", plugin.getConfigHandler().getPluginMCVersion()); // TODO: get server version

            toSend.append(line).append("&r\n");
        }

        Bukkit.getConsoleSender().sendMessage(plugin.translate(toSend.toString()));
    }

    public String getNearChatEnabled() {
        return childToMessage.getOrDefault("nearchat_enabled", "&c`nearchat_enabled`");
    }

    public String getNearChatEnabledPlayer(String playerName) {
        return childToMessage.getOrDefault("nearchat_enabled_player", "&c`nearchat_enabled_player`")
                .replace("%name%", playerName);
    }

    public String getNearChatDisabled() {
        return childToMessage.getOrDefault("nearchat_enabled", "&c`nearchat_enabled`");
    }

    public String getNearChatDisabledPlayer(String playerName) {
        return childToMessage.getOrDefault("nearchat_enabled_player", "&c`nearchat_enabled_player`")
                .replace("%name%", playerName);
    }

    public String getNearChatPlayerNotFound(String playerName) {
        return childToMessage.getOrDefault("nearchat_player_not_found", "&c`nearchat_player_not_found`")
                .replace("%name%", playerName);
    }

    public String getPlayerOnly() {
        return childToMessage.getOrDefault("player_only", "&c`player_only`");
    }

    public String getNoPermission() {
        return childToMessage.getOrDefault("no_permission", "&c`no_permission`");
    }

    public List<String> getHelpTextList() {
        return childToListMessage.getOrDefault("help_text", EMPTY_LIST);
    }

    public String getPrefix() {
        return childToMessage.getOrDefault("prefix", "&f[&6Near&eChat&f] &7");
    }

    public String getFormat() {
        return childToMessage.getOrDefault("format", "&c`format`");
    }

    public String getDBInit() {
        return modifyDBMessage(childToMessage.getOrDefault("db_init", "&c`db_init`"));
    }

    public String getDBConnected() {
        return modifyDBMessage(childToMessage.getOrDefault("db_connected", "&c`db_connected`"));
    }

    public String getDBConnectedConsole(String host) {
        return modifyDBMessage(childToMessage.getOrDefault("db_connected_console", "&c`db_connected_console`")
                .replace("%host%", host));
    }

    public String getDBDisconnected() {
        return modifyDBMessage(childToMessage.getOrDefault("db_disconnected", "&c`db_disconnected`"));
    }

    public String getDBErrorConnectUnsuccessful() {
        return modifyDBMessage(childToMessage.getOrDefault("db_error_connect_unsuccessful", "&c`db_error_connect_unsuccessful`"));
    }

    public String getDBErrorCredentialsNotFound() {
        return modifyDBMessage(childToMessage.getOrDefault("db_error_credentials_not_found", "&c`db_error_credentials_not_found`"));
    }

    public String getDBErrorConnectDisabled() {
        return modifyDBMessage(childToMessage.getOrDefault("db_error_connect_disabled", "&c`db_error_connect_disabled`"));
    }

    public String getDBErrorConnectedAlready() {
        return modifyDBMessage(childToMessage.getOrDefault("db_error_connect_already", "&c`db_error_connect_already`"));
    }

    public String getDBTrySave() {
        return modifyDBMessage(childToMessage.getOrDefault("db_try_save", "&c`db_try_save`"));
    }

    public String getDatabaseSaved() {
        return modifyDBMessage(childToMessage.getOrDefault("db_saved", "[%db_type%] &aSaved database."));
    }

    public String getReloadedConfig() {
        return modifyDBMessage(childToMessage.getOrDefault("reloaded_config", "&c`reloaded_config`"));
    }

    public String getCreatedFile(String file_name) {
        return childToMessage.getOrDefault("created_file",
                "&aSuccessfully created %file_name%").replace("%file_name%", file_name);
    }

    public String getBrokerStop(String broker) {
        return modifyDBMessage(childToMessage.getOrDefault("broker_stop", "&c`broker_stop`").replace("%broker%", broker));
    }

    public List<String> getEnabledMessage() {
        return childToListMessage.getOrDefault("enabled_message", EMPTY_LIST);
    }

    public List<String> getDisabledMessage() {
        return childToListMessage.getOrDefault("disabled_message", EMPTY_LIST);
    }

    public String modifyDBMessage(String db_message) {
        DatabaseAdapter db = plugin.getDatabaseHandler().getDatabase();

        if (db == null)
            return db_message != null ? db_message.replace("%db_type%", "NONE") : "null";
        else
            return db_message != null ? db_message.replace("%db_type%", db.toString()) : "null";
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