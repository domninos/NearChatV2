package net.omni.nearChat.database.adapters;

public interface DatabaseAdapter {

    void initDatabase();

    boolean connect();

    boolean isEnabled();

    void closeDatabase();
}
