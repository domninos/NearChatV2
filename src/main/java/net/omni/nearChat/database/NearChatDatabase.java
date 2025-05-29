package net.omni.nearChat.database;

public interface NearChatDatabase {
    void close();

    boolean isEnabled();

    enum Type {
        REDIS("redis"), FLAT_FILE("flat-file"), POSTGRESQL("postgresql");

        final String label;

        Type(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
