package net.omni.nearChat.handlers;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.database.NearChatDatabase;
import net.omni.nearChat.util.NearChatConfig;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigHandler {
    private final NearChatPlugin plugin;

    private NearChatConfig nearChatConfig;

    private String plugin_name, plugin_version, plugin_mc_version;

    private int database_save_delay;

    private int nearby_get_delay;

    private int near_block_radius;

    private String host, user, database_name, password;
    private int port;

    private boolean log_messages;

    private String database_type;

    private int delay_time;

    private boolean delay;

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
        this.database_type = getConfig().getString("database-type");
        this.delay_time = getConfig().getInt("delay-time");
        this.delay = getConfig().getBoolean("delay");

        this.host = getConfig().getString("host");
        this.port = getConfig().getInt("port");
        this.database_name = getConfig().getString("database-name");
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

        if (getConfig().getString("database-name") == null) {
            nearChatConfig.setNoSave("database-name", "<put database name here>");
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

        if (getConfig().getString("database-type") == null) {
            nearChatConfig.setNoSave("database-type", "flat-file");
            def = true;
        }

        if (getConfig().getInt("delay-time") == 0) {
            nearChatConfig.setNoSave("delay-time", 3);
            def = true;
        }

        if (getConfig().getString("delay") == null) {
            nearChatConfig.setNoSave("delay", true);
            def = true;
        }

        if (def) {
            plugin.sendConsole("&9Loaded default config values.");
            this.nearChatConfig.save();
        }

        return def;
    }

    public void setDatabase(NearChatDatabase.Type type) {
        nearChatConfig.set("database-type", type.getLabel());
        this.database_type = type.getLabel();
    }

    public int getDelayTime() {
        return delay_time;
    }

    public boolean isDelay() {
        return delay;
    }

    public void setDelayTime(int delay_time) {
        nearChatConfig.set("delay-time", delay_time);
        this.delay_time = delay_time;
    }

    public void setDelay(boolean delay) {
        nearChatConfig.set("delay", delay);
        this.delay = delay;
    }

    public NearChatDatabase.Type getDatabaseType() {
        try {
            return NearChatDatabase.Type.valueOf(this.database_type.toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException e) {
            plugin.error(plugin.getMessageHandler().getDBSwitchArg());
            return null;
        }
    }

    public boolean checkDev() {
        String devS = nearChatConfig.getString("dev");

        return devS != null && !devS.isBlank() && nearChatConfig.getBool("dev");
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

    public String getDatabaseName() {
        return database_name;
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
