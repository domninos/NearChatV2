package net.omni.nearChat.database;

public interface SQLCredentials {
    boolean connect(String host, int port, String database_name, String user, String password);
}
