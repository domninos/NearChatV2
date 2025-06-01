package net.omni.nearChat.database;

import java.util.Arrays;

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

        public static Type parseType(String label) {
            for (Type type : Type.values())
                if (type.getLabel().equalsIgnoreCase(label))
                    return type;

            return null;
        }

        public static String available() {
            return Arrays.toString(NearChatDatabase.Type.values())
                    .replace("_", "-").replace("[", "").replace("]", "");
        }
    }

}
