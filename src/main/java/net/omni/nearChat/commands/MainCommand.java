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

    public abstract String getPermission();

    public abstract void registerSubCommands();

    public abstract List<String> getHelpText();

    public void sendHelp(CommandSender sender) {
        StringBuilder toSend = new StringBuilder("\n");

        for (String line : getHelpText()) {
            if (line.isBlank()) {
                toSend.append("\n");
                continue;
            }

            String firstWord = line.split(" ")[0];

            if (firstWord.startsWith("<nearchat")) {
                // has permission check
                String permission = firstWord.replace("<", "").replace(">", "");

                if (sender.hasPermission(permission))
                    toSend.append(line.replace(firstWord, "").strip());

                continue;
            }

            if (line.contains("%plugin_name%"))
                line = line.replace("%plugin_name%", plugin.getConfigHandler().getPluginName());
            if (line.contains("%plugin_version%"))
                line = line.replace("%plugin_version%", plugin.getConfigHandler().getPluginVersion());
            if (line.contains("%plugin_mc_version%"))
                line = line.replace("%plugin_mc_version%", plugin.getConfigHandler().getPluginMCVersion());

            toSend.append(line.strip()).append("&r\n");
        }

        sender.sendMessage(plugin.translate(toSend.toString()));
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
