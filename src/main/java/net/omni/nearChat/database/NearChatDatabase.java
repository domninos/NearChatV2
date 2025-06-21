package net.omni.nearChat.database;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.util.Libraries;

import java.util.Arrays;

public interface NearChatDatabase {
    void close();

    boolean isEnabled();

    enum Type {
        REDIS("redis", Libraries.REDIS),
        FLAT_FILE("flat-file", Libraries.FLAT_FILE),
        POSTGRESQL("postgresql", Libraries.POSTGRESQL),
        SQLITE("sqlite", Libraries.SQLITE);

        private final String label;

        private final Libraries lib;

        Type(String label, Libraries lib) {
            this.label = label;
            this.lib = lib;
        }

        public String getLabel() {
            return label;
        }

        public boolean isLoaded(NearChatPlugin plugin) {
            return lib.isLoaded(plugin);
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
