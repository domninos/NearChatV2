package net.omni.nearChat.util;

import net.omni.nearChat.NearChatPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class DatabaseBroker extends BukkitRunnable {
    private final NearChatPlugin plugin;

    public DatabaseBroker(NearChatPlugin plugin) {
        this.plugin = plugin;

        runTaskTimerAsynchronously(plugin, 20 * 5 * 3, 20L); // 5 mins
    }

    @Override
    public void run() {
        plugin.getPlayerManager().saveToDatabase();
        plugin.sendConsole("&aSaved to database.");
    }
}
