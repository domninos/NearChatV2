package net.omni.nearChat.util;


import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerUtil {

    public static List<Player> getNearbyPlayers(Location loc, double base) {
        // REF: https://www.spigotmc.org/threads/performance-friendly-entity-finding.504599/

        List<Player> nearbyPlayers = new ArrayList<>();

        if (loc == null || loc.getWorld() == null)
            return nearbyPlayers;

        Chunk chunk = loc.getChunk();

        for (Player p : chunk.getPlayersSeeingChunk()) {
            if (p != null && p.getLocation().distanceSquared(loc) <= Math.pow(base, 2))
                nearbyPlayers.add(p);
        }

        return nearbyPlayers;
    }
}
