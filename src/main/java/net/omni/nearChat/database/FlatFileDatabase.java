package net.omni.nearChat.database;

import net.omni.nearChat.NearChatPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class FlatFileDatabase implements NearChatDatabase {
    private static final String FILE_NAME = "nearchat.txt";

    private final File file;
    private Map<String, Boolean> cache;
    private boolean enabled = false;

    private final NearChatPlugin plugin;

    public FlatFileDatabase(NearChatPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder().getPath() + "/" + FILE_NAME);
    }

    public void createFile() throws IOException {
        if (file.createNewFile())
            plugin.sendConsole(plugin.getMessageHandler().getCreatedFile(FILE_NAME));
    }

    public boolean connect() {
        this.cache = readFile();
        this.enabled = true;
        return true;
    }

    @Override
    public void close() {
        cache.clear();
        this.enabled = false;
    }

    public boolean has(String playerName) {
        return cache.containsKey(playerName);
    }

    public void put(String playerName, boolean value) {
        cache.put(playerName, value);
    }

    public boolean isEnabled() {
        return this.enabled && cache != null;
    }

    public boolean getValue(String playerName) {
        return cache.getOrDefault(playerName, false);
    }

    public Map<String, Boolean> readFile() {
        Map<String, Boolean> enabled = new HashMap<>();

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] split = line.split(":");

                // name: boolean
                // KimHeechul: true
                String name = split[0];
                boolean value = Boolean.parseBoolean(split[1].strip());

                enabled.put(name, value);
            }
        } catch (IOException e) {
            plugin.error("Something went wrong reading file: ", e);
        }

        return enabled;
    }

    public void writeToFile(String line) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(line + "\n");
        } catch (IOException e) {
            plugin.error("Something went wrong writing to file: ", e);
        }
    }
}
