package net.omni.nearChat.util;

import net.omni.nearChat.NearChatPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class NearChatConfig {

    private final NearChatPlugin plugin;
    private final File file;
    private final boolean res;
    private final String fileName;
    private FileConfiguration config;

    public NearChatConfig(NearChatPlugin plugin, String fileName) {
        this(plugin, fileName, plugin.getDataFolder(), false);
    }

    public NearChatConfig(NearChatPlugin plugin, String fileName, boolean res) {
        this(plugin, fileName, plugin.getDataFolder(), res);
    }

    public NearChatConfig(NearChatPlugin plugin, String fileName, File directory) {
        this(plugin, fileName, directory, false);
    }

    public NearChatConfig(NearChatPlugin plugin, String fileName, File directory, boolean res) {
        this.plugin = plugin;

        if (!fileName.endsWith(".yml"))
            fileName += ".yml";

        this.file = new File(directory, fileName);
        this.fileName = fileName;
        this.res = res;

        reload();
    }

    public void set(String path, Object obj) {
        set(path, obj, true);
    }

    public void set(String path, Object obj, boolean save) {
        config.set(path, obj);

        if (save)
            save();
    }

    public void setNoSave(String path, Object obj) {
        set(path, obj, false);
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.error("Something went wrong saving " + file.getName(), e);
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public int getInt(String path) {
        return config.getInt(path);
    }

    public boolean getBool(String path) {
        return config.getBoolean(path, false);
    }

    public void reload() {
        if (!file.exists()) {
            if (res) {
                plugin.saveResource(fileName, false);
                plugin.sendConsole(plugin.getMessageHandler().getCreatedFile(fileName));
            } else {
                try {
                    if (file.createNewFile())
                        plugin.sendConsole(plugin.getMessageHandler().getCreatedFile(fileName));
                } catch (IOException e) {
                    plugin.error("Something went wrong creating " + fileName, e);
                }
            }
        }

        this.config = YamlConfiguration.loadConfiguration(file);
    }
}