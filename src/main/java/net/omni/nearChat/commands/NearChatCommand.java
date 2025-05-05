package net.omni.nearChat.commands;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.commands.subcommands.DatabaseSubCommand;
import net.omni.nearChat.commands.subcommands.HelpSubCommand;
import net.omni.nearChat.commands.subcommands.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class NearChatCommand extends MainCommand {

    public NearChatCommand(NearChatPlugin plugin) {
        super(plugin);
    }

    @Override
    public void registerSubCommands() {
        subCommands.add(new DatabaseSubCommand(plugin, this));
        subCommands.add(new HelpSubCommand(plugin, this));
    }

    @Override
    public List<String> getHelpText() {
        return plugin.getMessageHandler().getHelpTextList();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (subCommands.isEmpty())
            return true;

        if (!sender.hasPermission(getPermission())) {
            plugin.sendMessage(sender, plugin.getMessageHandler().getNoPermission());
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                plugin.sendMessage(sender, plugin.getMessageHandler().getPlayerOnly());
                return true;
            }

            // TODO: open GUI with GUIHandler
        } else {
            for (SubCommand subCommand : getSubCommands()) {
                if (subCommand == null) continue;

                // TODO: tabbing / tabComplete()

                String perm = subCommand.getPermission();

                if (perm != null && !(sender.hasPermission(perm))) {
                    plugin.sendMessage(sender, plugin.getMessageHandler().getNoPermission());
                    return true;
                }

                String currentCmd = args[subCommand.getArg()];
                String subCmd = subCommand.getCommand();

                if (currentCmd.equalsIgnoreCase(subCmd) ||
                        Arrays.stream(subCommand.getAliases()).anyMatch((sub) -> sub.equalsIgnoreCase(currentCmd)))
                    return subCommand.execute(sender, args);
            }

            sendHelp(sender);
            return true;
        }

        sendHelp(sender);
        return true;
    }

    @Override
    public String getMainCommand() {
        return "nearchat";
    }

    @Override
    public String getPermission() {
        return "nearchat.use";
    }
}
