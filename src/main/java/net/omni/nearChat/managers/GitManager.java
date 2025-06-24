package net.omni.nearChat.managers;

import net.omni.nearChat.NearChatPlugin;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GitManager {

    protected static final String API_URL = "https://api.github.com/repos/domninos/NearChatV2/releases";

    private final NearChatPlugin plugin;

    public GitManager(NearChatPlugin plugin) {
        this.plugin = plugin;
    }

    public String getTagName() {
        try {
            HttpURLConnection http = getHttp(API_URL);

            if (http == null) {
                plugin.error("Unable to establish connection to API.");
                return "NULL";
            }

            if (http.getResponseCode() == 200)
                return getFromHTTP(http, "tag_name"); // v1.0.0-release || v1.0.0-alph
            else
                plugin.error("Something went wrong while checking for updates. HTTP Code: " + http.getResponseCode());
        } catch (IOException e) {
            plugin.error("Something went wrong while checking for updates.", e);
        }

        return "NULL";
    }

    private HttpURLConnection getHttp(String url) {
        try {
            HttpURLConnection http = (HttpURLConnection) new URL(url).openConnection();

            http.setRequestProperty("Accept", "application/vnd.github.v3+json");
            http.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");

            return http;
        } catch (IOException e) {
            plugin.error("Something went wrong getting http.", e);
        }

        return null;
    }

    private String getFromHTTP(HttpURLConnection http, String json_object) throws IOException {
        try (InputStreamReader inputStream = new InputStreamReader(http.getInputStream());
             BufferedReader reader = new BufferedReader(inputStream)) {
            StringBuilder response = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            JSONArray array = new JSONArray(response.toString());
            JSONObject recent_release = array.getJSONObject(array.length() - 1);

            return recent_release.getString(json_object);
        }
    }
}
