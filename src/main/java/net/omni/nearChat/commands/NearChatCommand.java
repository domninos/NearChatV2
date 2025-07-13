package net.omni.nearChat.commands;

import net.omc.config.value.ValueDef;
import net.omc.database.OMCDatabase;
import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.commands.subcommands.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NearChatCommand extends MainCommand {

    public NearChatCommand(NearChatPlugin plugin) {
        super(plugin);
    }

    @Override
    public void registerSubCommands() {
        subCommands.add(new HelpSubCommand(plugin, this));
        subCommands.add(new DatabaseSubCommand(plugin, this));
        subCommands.add(new ReloadSubCommand(plugin, this));
        subCommands.add(new DelaySubCommand(plugin, this));
        subCommands.add(new LicenseSubCommand(plugin, this));
    }

    @Override
    public List<String> getHelpText() {
        return plugin.getMessageHandler().getHelpTextList();
    }

    @Override
    public List<String> getTabCompleter(CommandSender sender, Command command, String label, String[] args) {
        if (subCommands.isEmpty())
            return ValueDef.EMPTY_LIST;

        if (!sender.hasPermission(getPermission()))
            return ValueDef.EMPTY_LIST;

        List<String> completer = new ArrayList<>();
        List<String> collection = new ArrayList<>();

        if (args.length == 0) {
            if (plugin.getDatabaseHandler().isEnabled()) {
                completer.add(getMainCommand());

                return StringUtil.copyPartialMatches(getMainCommand(), completer, collection);
            }
        } else {
            // check subcommands first

            if (args[0].equalsIgnoreCase("database") || args[0].equalsIgnoreCase("db")) {
                // /nc database switch <database> || /nc db switch <database>
                if (args.length == 2) {
                    // for "switch"

                    if (StringUtil.startsWithIgnoreCase("switch", args[1])) {
                        completer.add("switch");

                        StringUtil.copyPartialMatches(args[1].toLowerCase(), completer, collection);
                    }

                } else if (args.length == 3) {
                    // for databases
                    for (OMCDatabase.Type type : OMCDatabase.Type.values()) {
                        if (type == null)
                            continue;

                        if (StringUtil.startsWithIgnoreCase(type.getLabel(), args[2])) {
                            completer.add(type.getLabel());
                        }
                    }

                    StringUtil.copyPartialMatches(args[2], completer, collection);
                }
            }


            for (SubCommand subCommand : getSubCommands()) {
                if (subCommand == null) continue;

                String perm = subCommand.getPermission();

                if (perm != null && !sender.hasPermission(perm))
                    continue;

                int subArg = subCommand.getArg();
                String currentCmd = args[subArg];
                String subCmd = subCommand.getCommand();

                if (args.length != (subArg + 1)) // make sure to only tab complete when on the right place
                    continue;

                // just tabbed; empty
                if (currentCmd.isEmpty())
                    completer.add(subCmd);

                if (StringUtil.startsWithIgnoreCase(subCmd, currentCmd))
                    completer.add(subCmd);

                StringUtil.copyPartialMatches(currentCmd.toLowerCase(), completer, collection);
            }
        }

        return collection; // not empty
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
                plugin.sendMessage(sender, plugin.getDBMessageHandler().getDBErrorConnectDisabled());
                return true;
            }

            plugin.getPlayerManager().toggle(player);

            // TODO: open GUI with GUIHandler
        } else {
            // check subcommands first

            for (SubCommand subCommand : getSubCommands()) {
                if (subCommand == null) continue;

                String perm = subCommand.getPermission();

                if (perm != null && !(sender.hasPermission(perm))) {
                    plugin.sendMessage(sender, plugin.getMessageHandler().getNoPermission());
                    return true;
                }

                String currentCmd = args[subCommand.getArg()];
                String subCmd = subCommand.getCommand();

                if (
                        currentCmd.equalsIgnoreCase(subCmd)
                                || (subCommand.getAliases() != null
                                && Arrays.stream(subCommand.getAliases()).anyMatch((sub) -> sub.equalsIgnoreCase(currentCmd)))
                )
                    return subCommand.execute(sender, args);
            }

            // look for playername
            if (args.length == 1) {
                if (!plugin.getDatabaseHandler().isEnabled()) {
                    plugin.sendMessage(sender, plugin.getDBMessageHandler().getDBErrorConnectDisabled());
                    return true;
                }

                // permission

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
