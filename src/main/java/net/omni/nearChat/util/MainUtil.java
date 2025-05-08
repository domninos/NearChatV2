package net.omni.nearChat.util;

import java.util.Arrays;

public class MainUtil {

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
