package net.omni.nearChat.database.flatfile;

import net.omc.database.DatabaseAdapter;
import net.omc.database.flatfile.OMCFlatFileAdapter;
import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.handlers.DatabaseHandler;

import java.util.Map;

public class FlatFileAdapter extends OMCFlatFileAdapter {

    private final NearChatPlugin nearChatPlugin;

    public FlatFileAdapter(NearChatPlugin plugin, FlatFileDatabase database) {
        super(plugin, database);

        this.nearChatPlugin = plugin;
    }

    public static FlatFileAdapter from(DatabaseAdapter adapter) {
        return adapter instanceof FlatFileAdapter ? ((FlatFileAdapter) adapter) : null;
    }

    public static FlatFileAdapter adapt() {
        return from(DatabaseHandler.ADAPTER);
    }

    @Override
    public void initDatabase() {
        database.checkFile();
        connect();
    }

    @Override
    public void saveMap(Map<String, Boolean> enabledPlayers) {
        database.saveMap(enabledPlayers);
        plugin.sendConsole(plugin.getDBMessageHandler().getDatabaseSaved());
    }

    @Override
    public void savePlayer(String playerName, Boolean value) {
        database.savePlayer(playerName, value);
        // SAVE TO FILE
    }

    @Override
    public void lastSaveMap() {
        if (!database.isEnabled()) {
            plugin.error(plugin.getDBMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        Map<String, Boolean> enabledPlayers = nearChatPlugin.getPlayerManager().getEnabledPlayers();

        if (!enabledPlayers.isEmpty())
            saveMap(enabledPlayers);
    }


    @Override
    public void setToCache(String playerName) {
        nearChatPlugin.getPlayerManager().set(playerName, database.getValue(playerName));
    }
}
