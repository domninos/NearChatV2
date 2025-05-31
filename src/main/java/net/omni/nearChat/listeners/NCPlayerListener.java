package net.omni.nearChat.listeners;

import net.omni.nearChat.NearChatPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Set;

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
        if (!plugin.getDatabaseHandler().isEnabled()) return;

        Player player = event.getPlayer();

        // only save to database if in cache
        // only save if player executed /nearchat once
        if (plugin.getPlayerManager().has(player.getName())) {
            plugin.getPlayerManager().save(player.getName());
            plugin.getPlayerManager().removeNearby(player);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // i believe this was introduced on newer versions (1.18+ ?)

        if (!plugin.getDatabaseHandler().isEnabled()) return;

        Player player = event.getPlayer();

        if (!plugin.getPlayerManager().isEnabled(player.getName())) return;

        Set<Player> nearbyPlayers = plugin.getPlayerManager().getNearby(player);

        if (nearbyPlayers == null || nearbyPlayers.isEmpty())
            return;

        event.setCancelled(true);

        String format = plugin.getMessageHandler().getFormat();

        if (format.contains("%prefix%"))
            format = format.replace("%prefix%", plugin.getMessageHandler().getPrefix());
        if (format.contains("%player%"))
            format = format.replace("%player%", player.getDisplayName());
        if (format.contains("%chat%"))
            format = format.replace("%chat%", event.getMessage());

        format = plugin.translate(format);

        // TODO: FIX format
        String finalFormat = format;

        nearbyPlayers.forEach(nearbyPlayer -> nearbyPlayer.sendMessage(finalFormat));

        if (plugin.getConfigHandler().isLogging())
            Bukkit.getConsoleSender().sendMessage(format);
    }
}