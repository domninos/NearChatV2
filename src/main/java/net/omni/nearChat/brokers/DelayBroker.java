package net.omni.nearChat.brokers;

import net.omni.nearChat.NearChatPlugin;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class DelayBroker extends BukkitRunnable {
    private final NearChatPlugin plugin;

    public DelayBroker(NearChatPlugin plugin) {
        this.plugin = plugin;

        runTaskTimer(plugin, 20L, 20L * plugin.getConfigHandler().getDatabaseSaveDelay()); // sync
    }

    @Override
    public void run() {

        // TODO debug, not working

        try {
            for (Map.Entry<Player, Integer> delays : plugin.getPlayerManager().getDelays().entrySet()) {
                Player player = delays.getKey();
                int val = delays.getValue();

                if (val == 0) {
                    plugin.getPlayerManager().removeDelay(player);
                    plugin.sendConsole("delay removed for " + player.getName());
                } else {
                    delays.setValue(val - 1);
                    plugin.sendConsole("val - 1");
                }
            }
        } catch (Exception e) {
            plugin.error("Something went wrong during runtime of delay: ", e);
            cancel();
        }
    }
}
