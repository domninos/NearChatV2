package net.omni.nearChat.util;

import net.omni.nearChat.NearChatPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class NearChatConfig {

    private final NearChatPlugin plugin;
    private final File file;
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

        if (!file.exists()) {
            plugin.saveResource(fileName, false); // TODO: test
            plugin.sendConsole("&aSuccessfully created " + fileName);
        }

        reload();
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
            plugin.error(e);
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
        return config.getBoolean(path);
    }

    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(file);
    }
}