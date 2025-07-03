package net.omni.nearChat.commands;

import net.omc.database.OMCDatabase;
import net.omc.util.Flushable;
import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.commands.subcommands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

import java.util.ArrayList;
import java.util.List;

public abstract class MainCommand implements CommandExecutor, Flushable {

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

    public abstract List<String> getTabCompleter(CommandSender sender, Command command, String label, String[] args);

    public void sendHelp(CommandSender sender) {
        StringBuilder toSend = new StringBuilder("\n");

        for (String line : getHelpText()) {
            if (line.isBlank()) {
                toSend.append("\n");
                continue;
            }

            String firstWord = line.split(" ")[0];

            if (firstWord.startsWith("<nearchat.")) {
                // has permission check
                String permission = firstWord.replace("<", "").replace(">", "");

                if (sender.hasPermission(permission))
                    line = line.replace(firstWord, "").replaceFirst(" ", "");
                else
                    continue;
            }

            if (line.contains("%plugin_name%"))
                line = line.replace("%plugin_name%", plugin.getConfigHandler().getPluginName());
            if (line.contains("%plugin_version%"))
                line = line.replace("%plugin_version%", plugin.getConfigHandler().getPluginVersion());
            if (line.contains("%plugin_mc_version%"))
                line = line.replace("%plugin_mc_version%", plugin.getConfigHandler().getPluginMCVersion());

            if (line.contains("%databases%"))
                line = line.replace("%databases%", OMCDatabase.Type.available());

            toSend.append(line).append("&r\n");
        }

        sender.sendMessage(plugin.translate(toSend.toString()));
    }

    @Override
    public void flush() {
        subCommands.stream().filter(sub -> sub instanceof Flushable).forEach(sub -> ((Flushable) sub).flush());
        subCommands.clear();
    }

    public void register() {
        PluginCommand pc = Bukkit.getPluginCommand(getMainCommand());

        if (pc == null) {
            plugin.error("Could not register /" + getMainCommand() + " because it does not exist in plugin.yml!");
            return;
        }

        pc.setExecutor(this);
        pc.setTabCompleter(this::getTabCompleter);

        registerSubCommands();
        plugin.getCommands().add(this);

    }
}
