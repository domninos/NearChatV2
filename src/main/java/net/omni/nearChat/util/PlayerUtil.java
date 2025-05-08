package net.omni.nearChat.util;


import net.omni.nearChat.managers.PlayerManager;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class PlayerUtil {

    // returns nearby players who have nearchat enabled
    public static Set<Player> getNearbyPlayers(PlayerManager playerManager, Location loc, double base) {
        // REF: https://www.spigotmc.org/threads/performance-friendly-entity-finding.504599/

        Set<Player> nearbyPlayers = new HashSet<>();

        if (loc == null || loc.getWorld() == null)
            return nearbyPlayers;

        Chunk chunk = loc.getChunk();

        for (Player p : chunk.getPlayersSeeingChunk()) {
            if (p != null && playerManager.isEnabled(p.getName()) &&
                    p.getLocation().distanceSquared(loc) <= Math.pow(base, 2))
                nearbyPlayers.add(p);
        }

        return nearbyPlayers;
    }
}
