package net.omni.nearChat.commands;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.commands.subcommands.DatabaseSubCommand;
import net.omni.nearChat.commands.subcommands.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class NearChatCommand extends MainCommand {

    public NearChatCommand(NearChatPlugin plugin) {
        super(plugin);
    }

    @Override
    public void registerSubCommands() {
        subCommands.add(new DatabaseSubCommand(plugin, this));
    }

    @Override
    public String[] getHelpText() {
        return new String[]{
                "[+][+][+][+][+][+][+][+][+][+][+][+][+][+][+][+][+]",
                "&c/nearchat database >> Loads database from config.",
                "[+][+][+][+][+][+][+][+][+][+][+][+][+][+][+][+][+]",
        };
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
//        if (!(sender instanceof Player)) {
//            plugin.sendMessage(sender, plugin.getMessageHandler().getPlayersOnly());
//            return true;
//        }

        if (subCommands.isEmpty())
            return true;

        if (args.length == 0) {
            // TODO: Make separate class for each commands for cleanliness
            // TODO: open GUI
        } else {
            for (SubCommand subCommand : getSubCommands()) {
                if (subCommand == null) continue;

                if (args[subCommand.getArg()].equalsIgnoreCase(subCommand.getCommand()))
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
}
