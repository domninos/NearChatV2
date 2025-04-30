package net.omni.nearChat.commands;

import net.omni.nearChat.NearChatPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class NearChatCommand implements CommandExecutor {
    private final NearChatPlugin plugin;

    public NearChatCommand(NearChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, plugin.getMessageHandler().getPlayersOnly());
            return true;
        }

        if (args.length == 0) {
            // TODO: Make separate class for each commands for cleanliness
            // TODO: open GUI
        }
        return false;
    }

    public void register() {
        Objects.requireNonNull(Bukkit.getPluginCommand("nearchat")).setExecutor(this);
    }
}
