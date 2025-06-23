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

    private boolean update = false;
    private String versionUpdate = "NULL"; // NULL if no updates

    public VersionHandler(NearChatPlugin plugin) {
        this.plugin = plugin;
    }


    // TODO GitHub Release to check if there is an update
    //  check for private repositories

    public void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!Libraries.JSON.isLoaded(plugin)) // load JSON library
                    plugin.getLibraryHandler().ensureJSON().get();

                HttpURLConnection http = (HttpURLConnection) new URL(API_URL).openConnection();
                http.setRequestProperty("Accept", "application/vnd.github.v3+json");

                if (http.getResponseCode() == 200) {
                    String tagName = getTagName(http); // v1.0.0-release || v1.0.0-alpha

                    // TODO: uncomment this when releasing
//                    if (tagName.endsWith("alpha")) // ignore if it is pre-release (alpha)
//                        return;

                    String version = tagName.split("-")[0].substring(1, 6); // without the prefix 'v'

                    String fullVersion = plugin.getDescription().getVersion();
                    String currentVersion = fullVersion.split("-")[0]; // does not contain 'v'

                    this.versionUpdate = tagName;

                    int update = isUpdateFound(currentVersion, version);

                    if (update == 1) {
                        plugin.sendConsole("&eA new version is available! You're using v" + fullVersion + ". Please update to " + tagName + ".");
                        this.update = true;
                    } else if (update == -1)
                        plugin.sendConsole("&eYou are using an unstable version of the plugin (v" + currentVersion + " > v" + version + ").");

                } else
                    plugin.error("Something went wrong while checking for updates. HTTP Code: " + http.getResponseCode());
            } catch (IOException | ExecutionException | InterruptedException e) {
                plugin.error("Something went wrong while checking for updates.", e);
            }
        });
    }

    public String getVersionUpdate() {
        return this.versionUpdate;
    }

    public boolean hasUpdate() {
        return update;
    }

    private String getTagName(HttpURLConnection http) throws IOException {
        try (InputStreamReader inputStream = new InputStreamReader(http.getInputStream());
             BufferedReader reader = new BufferedReader(inputStream)) {
            StringBuilder response = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            JSONArray array = new JSONArray(response.toString());
            JSONObject recent_release = array.getJSONObject(array.length() - 1);

            return recent_release.getString("tag_name");
        }
    }

    private int isUpdateFound(String currentVersion, String releaseVersion) {
        String[] currentSplit = currentVersion.split("\\.");
        String[] releaseSplit = releaseVersion.split("\\.");

        int length = currentVersion.length();

        for (int i = 0; i < length; i++) {
            int num1 = i < currentSplit.length ? Integer.parseInt(currentSplit[i]) : 0;
            int num2 = i < releaseSplit.length ? Integer.parseInt(releaseSplit[i]) : 0;

            if (num1 < num2)
                return 1; // has a new update
            if (num1 > num2)
                return -1; // using a newer version than on GitHub release.
        }

        return 0; // equal
    }
}