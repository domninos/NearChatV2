package net.omni.nearChat.database.adapters;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.database.DatabaseHandler;

public class FlatFileAdapter implements DatabaseAdapter {
    private final boolean enabled = false;

    private final NearChatPlugin plugin;

    public FlatFileAdapter(NearChatPlugin plugin) {
        this.plugin = plugin;
    }

    public static FlatFileAdapter from(DatabaseAdapter adapter) {
        return adapter instanceof FlatFileAdapter ? ((FlatFileAdapter) adapter) : null;
    }

    public static FlatFileAdapter adapt() {
        return from(DatabaseHandler.ADAPTER);
    }

    @Override
    public void initDatabase() {

    }

    @Override
    public boolean connect() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void closeDatabase() {

    }
}
