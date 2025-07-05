package net.omni.nearChat.managers;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.omc.database.ISQLDatabase;
import net.omc.database.OMCDatabase;
import net.omc.util.MainUtil;
import net.omni.nearChat.NearChatPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PAPIManager {

    private final NearChatPlugin plugin;

    public PAPIManager(NearChatPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    public void checkPapi() {
        if (isEnabled())
            new NearChatExpansion(plugin).register();
    }

    private static class NearChatExpansion extends PlaceholderExpansion {
        private final NearChatPlugin plugin;

        public NearChatExpansion(NearChatPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public @NotNull String getIdentifier() {
            return "nc";
        }

        @Override
        public @NotNull String getAuthor() {
            return "domninos";
        }

        @Override
        public @NotNull String getVersion() {
            return plugin.getVersionManager().getCurrentVersion();
        }

        @Override
        public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
            switch (params.toLowerCase()) {
                case "db_type" -> {
                    return plugin.getDatabaseHandler().getAdapter().toString();
                }

                case "host" -> {
                    if (!plugin.getDatabaseHandler().isSQL())
                        return "N/A";

                    return ((ISQLDatabase) plugin.getDatabaseHandler().getAdapter().getDatabase()).getHost();
                }

                case "updates" -> {
                    return String.valueOf(plugin.getDatabaseHandler().getUpdates());
                }

                case "delay" -> {
                    if (player == null)
                        return String.valueOf(plugin.getConfigHandler().getDelayTime());
                    else if (plugin.getPlayerManager().hasDelay(player.getPlayer()))
                        return String.valueOf(plugin.getPlayerManager().getDelay(player.getPlayer()));
                }

                case "delay_on_join" -> {
                    return String.valueOf(plugin.getConfigHandler().getDelayTime());
                }

                case "databases" -> {
                    return OMCDatabase.Type.available();
                }

                case "db_delay" -> {
                    return String.valueOf(plugin.getConfigHandler().getDatabaseSaveDelay());
                }

                case "db_converted_ticks" -> {
                    return MainUtil.convertTicks(plugin.getConfigHandler().getDatabaseSaveDelay());
                }

                case "nearby_delay" -> {
                    return String.valueOf(plugin.getConfigHandler().getNearbyGetDelay());
                }

                case "nearby_converted_ticks" -> {
                    return MainUtil.convertTicks(plugin.getConfigHandler().getNearbyGetDelay());
                }

                case "radius" -> {
                    return String.valueOf(plugin.getConfigHandler().getNearBlockRadius());
                }

                default -> {
                    return "N/A";
                }
            }

            return "N/A";
        }

        @Override
        public boolean persist() {
            return true;
        }
    }
}