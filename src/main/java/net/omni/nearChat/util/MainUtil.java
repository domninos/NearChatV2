package net.omni.nearChat.util;

import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import net.omni.nearChat.NearChatPlugin;

import java.util.Arrays;

public class MainUtil {

    public static void loadLibraries(NearChatPlugin plugin) {
        // Create a library manager for a Bukkit/Spigot plugin
        // REF: https://github.com/AlessioDP/libby/tree/master

        BukkitLibraryManager libraryManager = new BukkitLibraryManager(plugin);

        // TODO load only when choosing sql
        // "{}" is replaced with ".", useful to avoid unwanted changes m ade by maven-shade-plugin

        Library lettuce = Library.builder()
                .groupId("io{}lettuce")
                .artifactId("lettuce-core")
                .version("6.6.0.RELEASE")
                .id("AlessioDP")
                .repository("https://repo.alessiodp.com/releases/")
                // Sets an id for the library
                // Relocation is applied to the downloaded jar before loading it
                .relocate("io{}lettuce{}core", "net{}omni{}nearChat{}libs{}io{}lettuce{}core")
                .build();

        Library reactive_streams = Library.builder()
                .groupId("org{}reactivestreams")
                .artifactId("reactive-streams")
                .version("1.0.4")
                .build();

        Library reactor_core = Library.builder()
                .groupId("io{}projectreactor")
                .artifactId("reactor-core")
                .version("3.6.6")
                .build();

        Library postgres = Library.builder()
                .groupId("org{}postgresql")
                .artifactId("postgresql")
                .version("42.7.5")
                .relocate("org{}postgresql", "net{}omni{}nearChat{}libs{}org{}postgresql")
                .build();
//
//        Library sqlite = Library.builder()
//                .groupId("org{}xerial")
//                .artifactId("sqlite-jdbc")
//                .version("3.49.1.0")
//                .relocate("org{}sqlite", "net{}omni{}nearChat{}libs{}org{}sqlite")
//                .build();

        Library hikaricp = Library.builder()
                .groupId("com{}zaxxer")
                .artifactId("HikariCP")
                .version("6.3.0")
                .relocate("com{}zaxxer{}hikari", "net{}omni{}nearChat{}libs{}com{}zaxxer{}hikari")
                .build();

        libraryManager.addMavenCentral();
        // TODO load only when choosing sql
        libraryManager.loadLibrary(reactor_core);
        libraryManager.loadLibrary(reactive_streams);
        libraryManager.loadLibrary(lettuce);

        libraryManager.loadLibrary(postgres);
//        libraryManager.loadLibrary(sqlite);
        libraryManager.loadLibrary(hikaricp);

        plugin.sendConsole("Loaded libraries"); // TODO messages.yml
    }

    public static String convertTicks(long ticks) {
        long millis = ticks * 50;
        long hours = millis / (1000 * 60 * 60);
        long minutes = (millis / (1000 * 60)) % 60;
        long seconds = (millis / 1000) % 60;

        return hours > 0 ? String.format("%02d hours", hours)
                : minutes > 0 ? String.format("%02d minutes", minutes)
                : seconds > 0 ? String.format("%02d seconds", seconds)
                : String.format("%d milliseconds", millis);
    }

    public static boolean isNullOrBlank(String... strings) {
        return Arrays.stream(strings).anyMatch(string -> string == null || string.isBlank());
    }
}
