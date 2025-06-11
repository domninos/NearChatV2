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
        if (!plugin.getDatabaseHandler().isEnabled())
            return;

        // cancels brokers if empty

        plugin.getPlayerManager().loadEnabled(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!plugin.getDatabaseHandler().isEnabled()) return;

        // cancels brokers if empty

        Player player = event.getPlayer();

        // only save to database if in cache
        // only save if player executed /nearchat once
        plugin.getPlayerManager().removeNearby(player);
        plugin.getPlayerManager().removeDelay(player);

        if (plugin.getPlayerManager().hasChanged(player.getName()))
            plugin.getPlayerManager().save(player.getName());

        plugin.getPlayerManager().removeInitial(player.getName());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // i believe this was introduced on newer versions (1.18+ ?)

        if (!plugin.getDatabaseHandler().isEnabled()) return;

        Player player = event.getPlayer();

        if (!plugin.getPlayerManager().isEnabled(player.getName())) return;

        if (plugin.getPlayerManager().hasDelay(player)) {
            int delay = plugin.getPlayerManager().getDelay(player);

            if (delay != 0) {
                event.setCancelled(true);
                plugin.sendMessage(player, plugin.getMessageHandler().getWaitDelay(delay));
                return;
            } else
                plugin.getPlayerManager().removeDelay(player);
        }

        String format = plugin.getMessageHandler().getFormat();

        if (format.contains("%prefix%"))
            format = format.replace("%prefix%", plugin.getMessageHandler().getPrefix());
        if (format.contains("%player%"))
            format = format.replace("%player%", player.getDisplayName());
        if (format.contains("%chat%"))
            format = format.replace("%chat%", event.getMessage());

        format = plugin.translate(format);

        String finalFormat = format;

        Set<Player> nearbyPlayers = plugin.getPlayerManager().getNearby(player);

        if (nearbyPlayers != null && !nearbyPlayers.isEmpty())
            nearbyPlayers.forEach(nearbyPlayer -> nearbyPlayer.sendMessage(finalFormat));

        player.sendMessage(finalFormat);

        if (plugin.getConfigHandler().isLogging())
            Bukkit.getConsoleSender().sendMessage(format);

        event.setCancelled(true);
    }
}