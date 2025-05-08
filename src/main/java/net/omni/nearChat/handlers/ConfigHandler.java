package net.omni.nearChat.handlers;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.util.NearChatConfig;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigHandler {
    private final NearChatPlugin plugin;

    private NearChatConfig nearChatConfig;

    private String plugin_name, plugin_version, plugin_mc_version;

    private int database_save_delay;

    private int nearby_get_delay;

    private int near_block_radius;

    private String host, user, password;
    private int port;

    private boolean log_messages;

    private boolean use_flat_file;

    public ConfigHandler(NearChatPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        if (this.nearChatConfig == null)
            this.nearChatConfig = plugin.getNearConfig();

        boolean def = loadDefaults(); // check if there are empty messages, if so, replace it

        // load messages to cache
        this.nearby_get_delay = getConfig().getInt("nearby-get-delay");
        this.database_save_delay = getConfig().getInt("database-save-delay");
        this.near_block_radius = getConfig().getInt("near-block-radius");

        this.log_messages = getConfig().getBoolean("log-messages");
        this.use_flat_file = getConfig().getBoolean("use-flat-file");

        this.host = getConfig().getString("host");
        this.port = getConfig().getInt("port");
        this.user = getConfig().getString("user");
        this.password = getConfig().getString("password");

        this.plugin_name = plugin.getDescription().getName();
        this.plugin_version = plugin.getDescription().getVersion();
        this.plugin_mc_version = plugin.getDescription().getAPIVersion();

        if (!def)
            plugin.sendConsole("&aLoaded config.");
    }

    public void saveToConfig() {
        nearChatConfig.setNoSave("host", this.host);
        nearChatConfig.setNoSave("port", this.port);
        nearChatConfig.setNoSave("user", this.user);
        nearChatConfig.setNoSave("password", this.password);

        nearChatConfig.setNoSave("nearby-get-delay", this.nearby_get_delay);
        nearChatConfig.setNoSave("database-save-delay", this.database_save_delay);
        nearChatConfig.setNoSave("near-block-radius", this.near_block_radius);
        nearChatConfig.setNoSave("log-messages", this.log_messages);

        nearChatConfig.save();
    }

    private boolean loadDefaults() {
        boolean def = false;

        if (getConfig().getString("host") == null) {
            nearChatConfig.setNoSave("host", "<put database host here>");
            def = true;
        }

        if (getConfig().getString("user") == null) {
            nearChatConfig.setNoSave("user", "<put database user here>");
            def = true;
        }

        if (getConfig().getString("password") == null) {
            nearChatConfig.setNoSave("password", "<put database password here>");
            def = true;
        }

        if (getConfig().getInt("port") == 0) {
            nearChatConfig.setNoSave("port", 0);
            def = true;
        }

        if (getConfig().getInt("near-block-radius") == 0) {
            nearChatConfig.setNoSave("near-block-radius", 30);
            def = true;
        }

        if (getConfig().getInt("database-save-delay") == 0) {
            nearChatConfig.setNoSave("database-save-delay", 6000); // 5 min default
            def = true;
        }

        if (getConfig().getInt("nearby-get-delay") == 0) {
            nearChatConfig.setNoSave("nearby-get-delay", 10);
            def = true;
        }

        if (getConfig().getString("log-messages") == null) {
            nearChatConfig.setNoSave("log-messages", true);
            def = true;
        }

        if (getConfig().getString("use-flat-file") == null) {
            nearChatConfig.setNoSave("use-flat-file", false);
            def = true;
        }

        if (def) {
            plugin.sendConsole("&9Loaded default config values.");
            this.nearChatConfig.save();
        }

        return def;
    }

    public boolean isFlatFile() {
        return use_flat_file;
    }

    public boolean isLogging() {
        return log_messages;
    }

    public int getNearbyGetDelay() {
        return nearby_get_delay;
    }

    public String getPluginName() {
        return plugin_name;
    }

    public String getPluginVersion() {
        return plugin_version;
    }

    public String getPluginMCVersion() {
        return plugin_mc_version;
    }

    public int getDatabaseSaveDelay() {
        return database_save_delay;
    }

    public int getNearBlockRadius() {
        return near_block_radius;
    }

    public String getHost() {
        return host;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    public FileConfiguration getConfig() {
        return nearChatConfig.getConfig();
    }
}
