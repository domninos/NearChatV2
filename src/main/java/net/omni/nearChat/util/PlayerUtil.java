package net.omni.nearChat.util;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerUtil {

    public static List<Player> getNearbyPlayers(Location loc, double base) {
        // REF: https://www.spigotmc.org/threads/performance-friendly-entity-finding.504599/

        if (loc == null || loc.getWorld() == null)
            return null;

        World world = loc.getWorld();
        int chunkRadius = (int) Math.ceil(base / 16);
        int minX = loc.getBlockX() - chunkRadius;
        int minZ = loc.getBlockZ() - chunkRadius;

        List<Player> nearbyPlayers = new ArrayList<>();

        for (int x = minX; x < minX + 2 * chunkRadius; x++) {
            for (int z = minZ; z < minZ + 2 * chunkRadius; z++) {
                Chunk chunk = world.getChunkAt(x, z);

                for (Entity entity : chunk.getEntities())
                    if (entity instanceof Player player)
                        if (player.getLocation().distanceSquared(loc) >= base * base)
                            nearbyPlayers.add(player);
            }
        }

        return nearbyPlayers;
    }
}
