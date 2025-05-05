package net.omni.nearChat.util.brokers;

import net.omni.nearChat.NearChatPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class DatabaseBroker extends BukkitRunnable {
    private final NearChatPlugin plugin;

    public DatabaseBroker(NearChatPlugin plugin) {
        this.plugin = plugin;

        runTaskTimerAsynchronously(plugin, 20L,
                plugin.getConfigHandler().getDatabaseSaveDelay());
    }

    @Override
    public void run() {
        try {
            if (plugin.getDatabaseHandler().isEnabled())
                plugin.getPlayerManager().saveToDatabase();
            else {
                plugin.error("Database disabled. Cancelling database broker..");
                cancel();
            }
        } catch (Exception e) {
            plugin.error(e);
        }
    }
}
