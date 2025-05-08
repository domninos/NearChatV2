package net.omni.nearChat.managers.brokers;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.util.PlayerUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Set;

public class NearbyBroker extends BukkitRunnable {
    private final NearChatPlugin plugin;

    public NearbyBroker(NearChatPlugin plugin) {
        this.plugin = plugin;

        runTaskTimerAsynchronously(plugin, 20L, plugin.getConfigHandler().getNearbyGetDelay());
    }

    @Override
    public void run() {
        try {
            if (!plugin.getDatabaseHandler().isEnabled()) {
                plugin.error(plugin.getMessageHandler().getBrokerStop("nearby"));
                cancel();
                return;
            }

            for (Map.Entry<Player, Set<Player>> entry : plugin.getPlayerManager().getNearbyPlayers().entrySet()) {
                Player key = entry.getKey();
                if (key == null) continue;
                if (!plugin.getPlayerManager().isEnabled(key.getName())) continue;

                Location playerLoc = key.getLocation();
                int block_radius = plugin.getConfigHandler().getNearBlockRadius();

                Set<Player> nearbyPlayers = PlayerUtil.getNearbyPlayers(plugin.getPlayerManager(), playerLoc, block_radius);

                entry.setValue(nearbyPlayers);
            }

        } catch (Exception e) {
            plugin.error("Something went wrong during runtime of nearby: ", e);
        }
    }
}