package net.omni.nearChat.brokers;

import net.omni.nearChat.NearChatPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class DatabaseBroker extends BukkitRunnable {
    private final NearChatPlugin plugin;

    public DatabaseBroker(NearChatPlugin plugin) {
        this.plugin = plugin;

        runTaskTimerAsynchronously(plugin, 20L, plugin.getConfigHandler().getDatabaseSaveDelay());
    }

    @Override
    public void run() {
        try {
            plugin.sendConsole(plugin.getMessageHandler().getDBTrySave());

            if (plugin.getDatabaseHandler().isEnabled())
                plugin.getPlayerManager().saveMap(true);
            else {
                plugin.sendConsole(plugin.getMessageHandler().getBrokerStop("database"));
                cancel();
            }
        } catch (Exception e) {
            plugin.error("Something went wrong during runtime of database: ", e);
        }
    }
}
