package net.omni.nearChat.handlers;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.util.Libraries;
import org.bukkit.Bukkit;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class VersionHandler {
    private static final String API_URL = "https://api.github.com/repos/domninos/NearChatV2/releases";

    private final NearChatPlugin plugin;

    public VersionHandler(NearChatPlugin plugin) {
        this.plugin = plugin;
    }

    // TODO


    // TODO GitHub Release to check if there is an update
    // TODO check if Gson is more lightweight than JSON

    public void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!Libraries.JSON.isLoaded(plugin)) // load JSON library
                    plugin.getLibraryHandler().ensureJSON().get();

                HttpURLConnection http = (HttpURLConnection) new URL(API_URL).openConnection();
                http.setRequestProperty("Accept", "application/vnd.github.v3+json"); // ??

                if (http.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream()));
                    StringBuilder response = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    JSONArray array = new JSONArray(response.toString());

                    plugin.sendConsole(line);

                    JSONObject release = new JSONObject(response.toString());
                    String tagName = release.getString("tag_name"); // latest

                    plugin.sendConsole("tag name = " + tagName);
                    plugin.sendConsole("plugin.yml version = " + plugin.getDescription().getVersion());
                } else
                    plugin.error("Something went wrong while checking for updates. HTTP Code: " + http.getResponseCode());
            } catch (IOException | ExecutionException | InterruptedException e) {
                plugin.error("Something went wrong while checking for updates.", e);
            }
        });
    }
}