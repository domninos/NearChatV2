package net.omni.nearChat.managers;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.database.ISQLDatabase;
import net.omni.nearChat.database.NearChatDatabase;
import net.omni.nearChat.util.MainUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PAPIManager {

    private final NearChatPlugin plugin;

    public PAPIManager(NearChatPlugin plugin) {
        this.plugin = plugin;
    }

    public void checkPapi() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
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
            return plugin.getVersionHandler().getCurrentVersion();
        }

        @Override
        public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
            switch (params.toLowerCase()) {
                case "db_type" -> {
                    return plugin.getDatabaseHandler().getAdapter().toString();
                }

                case "host" -> {
                    if (!plugin.getDatabaseHandler().isSQL())
                        return null;

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
                    return NearChatDatabase.Type.available();
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
                    return null;
                }
            }

            return null;
        }

        @Override
        public boolean persist() {
            return true;
        }
    }
}