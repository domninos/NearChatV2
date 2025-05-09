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

        Library lettuce = Library.builder()
                .groupId("io{}lettuce") // "{}" is replaced with ".", useful to avoid unwanted changes made by maven-shade-plugin
                .artifactId("lettuce-core")
                .version("6.5.5.RELEASE")
                .id("AlessioDP")
                .repository("https://repo.alessiodp.com/releases/")
                // The following are optional

                // Sets an id for the library
                // Relocation is applied to the downloaded jar before loading it
                .relocate("io{}lettuce{}core", "net{}omni{}nearChat{}libs{}io{}lettuce{}core") // "{}" is replaced with ".", useful to avoid unwanted changes made by maven-shade-plugin
                .build();

        Library reactive_streams = Library.builder()
                .groupId("org{}reactivestreams")
                .artifactId("reactive-streams")
                .version("1.0.4")
                .build();


        Library reactor_core = Library.builder()
                .groupId("io{}projectreactor")
                .artifactId("reactor-core")
                .version("3.6.4")
                .build();

        libraryManager.addMavenCentral();
        libraryManager.loadLibrary(reactor_core);
        libraryManager.loadLibrary(reactive_streams);
        libraryManager.loadLibrary(lettuce);
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
