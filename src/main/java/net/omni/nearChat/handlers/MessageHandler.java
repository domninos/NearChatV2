package net.omni.nearChat.handlers;

import net.omc.config.ConfigAbstract;
import net.omc.config.value.ValueType;
import net.omc.database.OMCDatabase;
import net.omc.util.MainUtil;
import net.omni.nearChat.NearChatPlugin;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.List;

public class MessageHandler extends ConfigAbstract {

    public static final List<String> EMPTY_LIST = List.of();

    private final NearChatPlugin nearChatPlugin;

    public MessageHandler(NearChatPlugin plugin) {
        super(plugin);
        this.nearChatPlugin = plugin;
    }

    @Override
    public void initialize() {
        flush();

        // load messages to cache
        builder.fromConfig()
                .load("player_only", ValueType.STRING, "&cOnly players can execute this command.")
                .load("no_permission", ValueType.STRING, "&cYou do not have permission to use this command.")

                .load("nearchat_enabled", ValueType.STRING, "&aEnabled")
                .load("nearchat_enabled_player", ValueType.STRING, "&aEnabled NearChat for player %player%")
                .load("nearchat_disabled", ValueType.STRING, "&cDisabled")
                .load("nearchat_disabled_player", ValueType.STRING, "&cDisabled NearChat for player %player%")
                .load("nearchat_player_not_found", ValueType.STRING, "&cCould not find player %player%")

                .load("reloaded_config", ValueType.STRING, "&aReloaded config and messages.yml")

                .load("prefix", ValueType.STRING, "&f[&6Near&eChat&f]&7")
                .load("format", ValueType.STRING, "%prefix% %player%&r: %chat%")

                .load("broker_stop", ValueType.STRING, "&cDatabase disabled. Cancelling %broker% broker..")
                .load("broker_empty_cancel", ValueType.STRING, "[%db_type%] &cThere are no players using nearchat. Cancelled the %broker% broker.")
                .load("broker_cancel", ValueType.STRING, "&cCancelled the %broker% broker.")

                .load("wait_delay", ValueType.STRING, "&cYou must wait %delay% seconds to chat in NearChat.")
                .load("delay_switch_on", ValueType.STRING, "&aSuccessfully enabled delay.")
                .load("delay_switch_off", ValueType.STRING, "&cSuccessfully disabled delay")
                .load("delay_set", ValueType.STRING, "&aSuccessfully set delay to %delay% seconds")

                .load("library_loaded", ValueType.STRING, "&aLoaded %library% libraries.")
                .load("library_downloading", ValueType.STRING, "&aDownloading libraries. Please wait for a few seconds.")

                .load("help_text", ValueType.STRING_LIST, Arrays.asList(
                        "&7-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-",
                        "&a/nearchat [nc] » Opens the NearChat GUI.",
                        "&a/nc help » Opens this menu.",
                        "<nearchat.db> &a/nc database [db] » Reconnects to the database.",
                        "<nearchat.db.switch> &a/nc database switch <database> » Switch database. &c(DISCOURAGED)",
                        "<nearchat.reload> &a/nc reload [rl] » Reloads config.yml and messages.yml.",
                        "<nearchat.delay> &a/nc delay » Toggle delay.",
                        "<nearchat.delay> &a/nc delay <time> » Set delay time.",
                        "",
                        "<nearchat.db.switch> &7Available Databases: %databases%",
                        "",
                        "&bDISCORD: discord.gg/nearchat",
                        "",
                        "&6%plugin_name% running %plugin_version% for MC %plugin_mc_version%",
                        "&7-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-"
                ))

                .load("enabled_message", ValueType.STRING_LIST, Arrays.asList(
                        "",
                        "&aSuccessfully enabled %plugin_name% [%plugin_mc_version%]",
                        "&bSettings:"))

                .load("settings_message", ValueType.STRING_LIST, Arrays.asList(
                        "&bSettings:",
                        "  &dDatabase: %db_type%",
                        "  &dDatabase Saving Delay: %db_delay%ms (%db_converted_ticks%)",
                        "  &dNearby Get Delay: %nearby_delay%ms (%nearby_converted_ticks%)",
                        "  &dNearby Radius: %radius% blocks",
                        "  &dDelay: %delay_on_join% seconds"))

                .load("disabled_message", ValueType.STRING_LIST, List.of(
                        "%prefix%&cSuccessfully disabled &6%plugin_name% &c[%plugin_version%]"))


                .save();

    }

    public void saveToConfig() {
        builder.fromConfig().saveAll();
    }

    /*
    TODO: fix spacing when logging on paper 1.11.2 and lower
     */
    public void sendEnabledMessage() {
        for (String line : getEnabledMessage()) {
            if (line.contains("%plugin_name%"))
                line = line.replace("%plugin_name%", nearChatPlugin.getConfigHandler().getPluginName());
            if (line.contains("%plugin_version%"))
                line = line.replace("%plugin_version%", nearChatPlugin.getConfigHandler().getPluginVersion());
            if (line.contains("%plugin_mc_version%"))
                line = line.replace("%plugin_mc_version%", nearChatPlugin.getConfigHandler().getPluginMCVersion());

            if (line.contains("%settings%")) {
                sendSettings();
                continue;
            }

            Bukkit.getConsoleSender().sendMessage(plugin.translate(line));
        }
    }

    public void sendSettings() {
        for (String line : getSettingsMessage()) {
            if (line.contains("%db_type%"))
                line = modifyDBMessage(line);
            if (line.contains("%db_delay%"))
                line = line.replace("%db_delay%",
                        String.valueOf(nearChatPlugin.getConfigHandler().getDatabaseSaveDelay()));
            if (line.contains("%db_converted_ticks%"))
                line = line.replace("%db_converted_ticks%",
                        MainUtil.convertTicks(nearChatPlugin.getConfigHandler().getDatabaseSaveDelay()));
            if (line.contains("%nearby_delay%"))
                line = line.replace("%nearby_delay%",
                        String.valueOf(nearChatPlugin.getConfigHandler().getNearbyGetDelay()));
            if (line.contains("%nearby_converted_ticks%"))
                line = line.replace("%nearby_converted_ticks%",
                        MainUtil.convertTicks(nearChatPlugin.getConfigHandler().getNearbyGetDelay()));
            if (line.contains("%radius%"))
                line = line.replace("%radius%",
                        String.valueOf(nearChatPlugin.getConfigHandler().getNearBlockRadius()));
            if (line.contains("%delay_on_join%"))
                line = line.replace("%delay_on_join%",
                        String.valueOf(nearChatPlugin.getConfigHandler().getDelayTime()));

            Bukkit.getConsoleSender().sendMessage(plugin.translate(line));
        }
    }

    public void sendDisabledMessage() {
        for (String line : getDisabledMessage()) {
            if (line.contains("%prefix%"))
                line = line.replace("%prefix%", getPrefix());
            if (line.contains("%plugin_name%"))
                line = line.replace("%plugin_name%", nearChatPlugin.getConfigHandler().getPluginName());
            if (line.contains("%plugin_mc_version%"))
                line = line.replace("%plugin_mc_version%", nearChatPlugin.getConfigHandler().getPluginMCVersion());
            if (line.contains("%plugin_version%"))
                line = line.replace("%plugin_version%", nearChatPlugin.getConfigHandler().getPluginVersion());

            Bukkit.getConsoleSender().sendMessage(plugin.translate(line));
        }
    }

    public String getNearChatEnabled() {
        return getString("nearchat_enabled");
    }

    public String getNearChatEnabledPlayer(String playerName) {
        return getString("nearchat_enabled_player").replace("%player%", playerName);
    }

    public String getNearChatDisabled() {
        return getString("nearchat_disabled");
    }

    public String getNearChatDisabledPlayer(String playerName) {
        return getString("nearchat_disabled_player").replace("%player%", playerName);
    }

    public String getNearChatPlayerNotFound(String playerName) {
        return getString("nearchat_player_not_found").replace("%player%", playerName);
    }

    public String getPlayerOnly() {
        return getString("player_only");
    }

    public String getNoPermission() {
        return getString("no_permission");
    }

    public List<String> getHelpTextList() {
        return getStringList("help_text");
    }

    public String getPrefix() {
        return getString("prefix");
    }

    public String getFormat() {
        return getString("format");
    }

    public String getReloadedConfig() {
        return modifyDBMessage(getString("reloaded_config"));
    }

    public String getBrokerStop(String broker) {
        return modifyDBMessage(getString("broker_stop").replace("%broker%", broker));
    }

    public String getBrokerEmptyCancel(String broker) {
        return modifyDBMessage(getString("broker_empty_cancel").replace("%broker%", broker));
    }

    public String getBrokerCancel(String broker) {
        return modifyDBMessage(getString("broker_cancel").replace("%broker%", broker));
    }

    public String getWaitDelay(int delay) {
        String seconds = delay == 1 ? delay + " second" : delay + " seconds";

        return getString("wait_delay").replace("%delay%", seconds);
    }

    public String getDelaySwitch(boolean delay_switch) {
        return getString(delay_switch ? "delay_switch_on" : "delay_switch_off");
    }

    public String getDelaySet(int set) {
        return getString("delay_set").replace("%delay%", String.valueOf(set));
    }

    public String getLibraryLoaded(String library) {
        return getString("library_downloading").replace("%library%", library);
    }

    public String getLibraryDownloading() {
        return getString("library_downloading");
    }

    public List<String> getEnabledMessage() {
        return getStringList("enabled_message");
    }

    public List<String> getSettingsMessage() {
        return getStringList("settings_message");
    }

    public List<String> getDisabledMessage() {
        return getStringList("disabled_message");
    }

    private String modifyDBMessage(String message) {
        OMCDatabase.Type type = nearChatPlugin.getConfigHandler().getDatabaseType();

        return type != null ? plugin.translate(message.replace("%db_type%", type.getLabel())) : "NONE";
    }

}