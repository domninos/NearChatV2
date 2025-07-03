package net.omni.nearChat.database.postgres;

import net.omc.database.postgres.OMCPostgresDatabase;
import net.omni.nearChat.NearChatPlugin;


public class PostgresDatabase extends OMCPostgresDatabase {
    private final NearChatPlugin nearChatPlugin;

    public PostgresDatabase(NearChatPlugin plugin) {
        super(plugin, "nearchat_enabled");

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
