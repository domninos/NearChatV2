package net.omni.nearChat.handlers;

import net.omc.util.Libraries;
import net.omc.util.MainUtil;
import net.omni.nearChat.NearChatPlugin;
import org.bukkit.Bukkit;

import java.util.concurrent.ExecutionException;

public class VersionHandler {
    private final NearChatPlugin plugin;

    private boolean update = false;
    private String versionUpdate = "NULL"; // NULL if no updates

    private String currentVersion;

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

                String tagName = plugin.getGitManager().getTagName(); // v1.0.0-release || v1.0.0-alpha

                if (tagName.equals("NULL")) {
                    plugin.error("Something went wrong getting tag name.");
                    return;
                }

                // TODO: uncomment this when releasing
//                    if (tagName.endsWith("alpha")) // ignore if it is pre-release (alpha)
//                        return;

                String version = tagName.split("-")[0].substring(1, 6); // without the prefix 'v'

                String fullVersion = plugin.getDescription().getVersion();
                String currentVersion = fullVersion.split("-")[0]; // does not contain 'v'

                this.versionUpdate = tagName;

                int update = MainUtil.isUpdateFound(currentVersion, version);

                if (update == 1) {
                    plugin.sendConsole("&eA new version is available! You're using v" + fullVersion + ". Please update to " + tagName + ".");
                    this.update = true;
                } else if (update == -1)
                    plugin.sendConsole("&eYou are using an unstable version of the plugin (v" + currentVersion + " > v" + version + ").");
            } catch (ExecutionException | InterruptedException e) {
                plugin.error("Something went wrong while checking for updates.", e);
            }
        });
    }

    public String getCurrentVersion() {
        return this.currentVersion == null ? this.currentVersion = plugin.getDescription().getVersion() : this.currentVersion;
    }

    public String getVersionUpdate() {
        return this.versionUpdate;
    }

    public boolean hasUpdate() {
        return update;
    }
}