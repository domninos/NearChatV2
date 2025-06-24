package net.omni.nearChat.util;

import org.bukkit.Bukkit;

import java.util.Arrays;

public class MainUtil {
    public static int VERSION;
    public static String FULL_VERSION;

    static {
        String major = Bukkit.getServer().getBukkitVersion().split("-")[0];

        String[] majorSplit = major.split("\\."); // 1.21

        FULL_VERSION = major;
        VERSION = Integer.parseInt(majorSplit[1]); // 21
    }

    public static String convertTicks(long ticks) {
        long millis = ticks * 50;
        long hours = millis / (1000 * 60 * 60);
        long minutes = (millis / (1000 * 60)) % 60;
        long seconds = (millis / 1000) % 60;

        return hours > 0 ? String.format("%d hours", hours)
                : minutes > 0 ? String.format("%d minutes", minutes)
                : seconds > 0 ? (seconds == 1 ? String.format("%d second", seconds) : String.format("%d seconds", seconds))
                : String.format("%d milliseconds", millis);
    }

    public static boolean isNullOrBlank(String... strings) {
        return Arrays.stream(strings).anyMatch(string -> string == null || string.isBlank());
    }

    public static int isUpdateFound(String currentVersion, String releaseVersion) {
        String[] currentSplit = currentVersion.split("\\.");
        String[] releaseSplit = releaseVersion.split("\\.");

        int length = currentVersion.length();

        for (int i = 0; i < length; i++) {
            int num1 = i < currentSplit.length ? Integer.parseInt(currentSplit[i]) : 0;
            int num2 = i < releaseSplit.length ? Integer.parseInt(releaseSplit[i]) : 0;

            if (num1 < num2)
                return 1; // has a new update
            if (num1 > num2)
                return -1; // using a newer version than on GitHub release.
        }

        return 0; // equal
    }
}
