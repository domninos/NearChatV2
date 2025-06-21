package net.omni.nearChat.util;

import net.omni.nearChat.NearChatPlugin;

import java.io.File;

public enum Libraries {
    HIKARICP("com/zaxxer/HikariCP"),
    PROJECT_REACTOR("io/projectreactor"),
    REACTIVE_STREAMS("org/reactivestreams"),
    REDIS("io/lettuce/lettuce-core"),
    POSTGRESQL("org/postgresql"),
    SQLITE("org/xerial/sqlite-jdbc"),
    FLAT_FILE("");

    private final String path;

    private File directory;

    private boolean loaded = false;

    Libraries(String path) {
        this.path = path;
    }

    public void load(File pluginFolder) {
        this.loaded = true;

        if (directory == null)
            this.directory = new File(pluginFolder + "/lib/" + this.path);
    }

    public boolean isLoaded(NearChatPlugin plugin) {
        if (this == FLAT_FILE) // is flat file, no libraries
            return true;

        if (this == POSTGRESQL) {
            if (!HIKARICP.isLoaded(plugin))
                plugin.sendConsole("HikariCP is needed for connection pooling (PostgreSQL)");
        }

        return this.loaded;
    }
}
