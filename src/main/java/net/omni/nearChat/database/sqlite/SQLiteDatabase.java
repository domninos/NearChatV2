package net.omni.nearChat.database.sqlite;

import net.omc.database.sqlite.OMCSQLiteDatabase;
import net.omni.nearChat.NearChatPlugin;

public class SQLiteDatabase extends OMCSQLiteDatabase {
    private final NearChatPlugin nearChatPlugin;

    public SQLiteDatabase(NearChatPlugin plugin) {
        super(plugin, "nearchat.db", "nearchat_enabled");

        this.nearChatPlugin = plugin;
    }

    @Override
    public void saveNonExists(String playerName, Boolean value) {
        if (!isEnabled()) {
            plugin.error(plugin.getDBMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        insert(playerName, value);

        nearChatPlugin.getPlayerManager().setInitial(playerName, value);
    }
}