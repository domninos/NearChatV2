package net.omni.nearChat.database.postgres;

import net.omc.database.postgres.OMCPostgresAdapter;
import net.omni.nearChat.NearChatPlugin;

import java.util.Map;

public class PostgresAdapter extends OMCPostgresAdapter {

    private final NearChatPlugin nearChatPlugin;

    public PostgresAdapter(NearChatPlugin plugin, PostgresDatabase database) {
        super(plugin, database);

        this.nearChatPlugin = plugin;
    }

    @Override
    public void initDatabase() {
    }

    @Override
    public void saveMap(Map<String, Boolean> enabledPlayers) {
        try {
            if (!enabledPlayers.isEmpty())
                this.database.saveMap(enabledPlayers, true);

            plugin.sendConsole(plugin.getDBMessageHandler().getDatabaseSaved());
        } catch (Exception e) {
            plugin.error("Could not save database properly.", e);
        }
    }

    @Override
    public void lastSaveMap() {
        if (!database.isEnabled())
            return;

        try {
            Map<String, Boolean> enabledPlayers = nearChatPlugin.getPlayerManager().getEnabledPlayers();

            if (!enabledPlayers.isEmpty())
                this.database.saveMap(enabledPlayers, false);

            closeDatabase();
        } catch (Exception e) {
            plugin.error("Could not save database properly", e);
        }
    }

    @Override
    public void setToCache(String playerName) {
        boolean value = getValue(playerName);

        nearChatPlugin.getPlayerManager().set(playerName, value);
    }
}
