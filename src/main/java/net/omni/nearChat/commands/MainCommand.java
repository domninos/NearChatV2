package net.omni.nearChat.commands;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.commands.subcommands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

import java.util.ArrayList;
import java.util.List;

public abstract class MainCommand implements CommandExecutor {

    protected final List<SubCommand> subCommands = new ArrayList<>();
    protected final NearChatPlugin plugin;

    public MainCommand(NearChatPlugin plugin) {
        this.plugin = plugin;
    }

    public List<SubCommand> getSubCommands() {
        return this.subCommands;
    }

    public abstract String getMainCommand();

    public abstract void registerSubCommands();

    public abstract String[] getHelpText();

    public void sendHelp(CommandSender sender) {
        plugin.sendMessage(sender, String.join("&r\n", getHelpText()));
    }

    public void flush() {
        subCommands.clear();
    }

    public void register() {
        PluginCommand pc = Bukkit.getPluginCommand(getMainCommand());
        if (pc == null) {
            plugin.error("Could not register /" + getMainCommand() + " because it does not exist in plugin.yml!");
            return;
        }

        pc.setExecutor(this);
        registerSubCommands();
        plugin.getCommands().add(this);
    }
}
