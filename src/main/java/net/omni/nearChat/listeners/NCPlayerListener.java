package net.omni.nearChat.listeners;

import net.omni.nearChat.NearChatPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class NCPlayerListener implements Listener {
    private final NearChatPlugin plugin;

    public NCPlayerListener(NearChatPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.getDatabaseHandler().isEnabled())
            plugin.getPlayerManager().loadEnabled(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.getDatabaseHandler().isEnabled())
            plugin.getPlayerManager().saveToDatabase(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!plugin.getDatabaseHandler().isEnabled()) return;

        Player player = event.getPlayer();

        if (plugin.getPlayerManager().has(player.getUniqueId())) {
            int block_radius = plugin.getConfigHandler().getNearBlockRadius();

            for (Entity nearbyEntity : player.getNearbyEntities(block_radius, block_radius, block_radius)) {
                if (nearbyEntity instanceof Player nearbyPlayer && plugin.getPlayerManager().isEnabled(nearbyPlayer.getUniqueId()))
                    nearbyPlayer.sendMessage(event.getMessage());
            }
        }
    }
}
