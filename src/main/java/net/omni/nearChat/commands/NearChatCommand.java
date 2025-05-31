package net.omni.nearChat.commands;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.commands.subcommands.DatabaseSubCommand;
import net.omni.nearChat.commands.subcommands.HelpSubCommand;
import net.omni.nearChat.commands.subcommands.ReloadSubCommand;
import net.omni.nearChat.commands.subcommands.SubCommand;
import org.bukkit.Bukkit;
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
        subCommands.add(new ReloadSubCommand(plugin, this));
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
            if (!(sender instanceof Player player)) {
                plugin.sendMessage(sender, plugin.getMessageHandler().getPlayerOnly());
                return true;
            }
            if (!plugin.getDatabaseHandler().isEnabled()) {
                plugin.sendMessage(sender, plugin.getMessageHandler().getDBErrorConnectDisabled());
                return true;
            }

            plugin.getPlayerManager().toggle(player);

            // TODO: open GUI with GUIHandler
        } else {
            // check subcommands first

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

            // look for playername
            if (args.length == 1) {
                if (!plugin.getDatabaseHandler().isEnabled()) {
                    plugin.sendMessage(sender, plugin.getMessageHandler().getDBErrorConnectDisabled());
                    return true;
                }

                Player player = Bukkit.getPlayerExact(args[0]);

                if (player == null) {
                    plugin.sendMessage(sender, plugin.getMessageHandler().getNearChatPlayerNotFound(args[0]));
                    return true;
                }

                plugin.getPlayerManager().toggle(player, false);

                if (plugin.getPlayerManager().isEnabled(player.getName()))
                    plugin.sendMessage(sender, plugin.getMessageHandler().getNearChatEnabledPlayer(player.getName()));
                else
                    plugin.sendMessage(sender, plugin.getMessageHandler().getNearChatDisabledPlayer(player.getName()));

                return true;
            }

            sendHelp(sender);
            return true;
        }

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
