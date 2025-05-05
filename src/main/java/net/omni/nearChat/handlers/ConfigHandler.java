package net.omni.nearChat.handlers;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.util.NearChatConfig;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigHandler {
    private final NearChatPlugin plugin;

    private final NearChatConfig nearChatConfig;

    private int near_block_radius;
    private String host, user, password;

    private int port;

    public ConfigHandler(NearChatPlugin plugin) {
        this.plugin = plugin;
        this.nearChatConfig = plugin.getNearConfig();
    }

    public void load() {
        boolean def = loadDefaults(); // check if there are empty messages, if so, replace it

        // load messages to cache
        this.near_block_radius = getConfig().getInt("near-block-radius");

        this.host = getConfig().getString("host");
        this.port = getConfig().getInt("port");
        this.user = getConfig().getString("user");
        this.password = getConfig().getString("password");

        if (!def)
            plugin.sendConsole("&aLoaded config");
    }

    public void saveToConfig() {
        // TODO for host, port, user, password

        nearChatConfig.setNoSave("host", this.host);
        nearChatConfig.setNoSave("port", this.port);
        nearChatConfig.setNoSave("user", this.user);
        nearChatConfig.setNoSave("password", this.password);

        nearChatConfig.setNoSave("near-block-radius", this.near_block_radius);

        nearChatConfig.save();
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

        if (def) {
            plugin.sendConsole("&9Loaded default config values.");
            this.nearChatConfig.save();
        }

        return def;
    }

    public FileConfiguration getConfig() {
        return nearChatConfig.getConfig();
    }
}
