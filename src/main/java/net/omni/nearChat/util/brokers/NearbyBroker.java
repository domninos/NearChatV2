package net.omni.nearChat.util.brokers;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.util.PlayerUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;

public class NearbyBroker extends BukkitRunnable {
    private final NearChatPlugin plugin;

    public NearbyBroker(NearChatPlugin plugin) {
        this.plugin = plugin;

        runTaskTimerAsynchronously(plugin, 20L, 20 * 30); // every 30 seconds
    }

    @Override
    public void run() {
        try {
            if (!plugin.getDatabaseHandler().isEnabled()) {
                plugin.error("Database disabled. Cancelling nearby broker..");
                cancel();
                return;
            }

            for (Map.Entry<Player, List<Player>> entry : plugin.getPlayerManager().getNearbyPlayers().entrySet()) {
                Player key = entry.getKey();
                if (key == null) continue;

                if (!plugin.getPlayerManager().isEnabled(key.getName())) continue;

                Location playerLoc = key.getLocation();
                int block_radius = plugin.getConfigHandler().getNearBlockRadius();

                List<Player> nearbyPlayers = PlayerUtil.getNearbyPlayers(playerLoc, block_radius);

                entry.setValue(nearbyPlayers);
            }

        } catch (Exception e) {
            plugin.error(e);
        }
    }
}