package net.omni.nearChat.commands.subcommands;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.brokers.NCBroker;
import net.omni.nearChat.commands.MainCommand;
import org.bukkit.command.CommandSender;

public class DelaySubCommand extends SubCommand {
    public DelaySubCommand(NearChatPlugin plugin, MainCommand mainCommand) {
        super(plugin, mainCommand);
    }

    @Override
    public String getCommand() {
        return "delay";
    }

    @Override
    public String[] getAliases() {
        return null;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!plugin.getDatabaseHandler().isEnabled()) {
            plugin.sendMessage(sender, plugin.getMessageHandler().getDBErrorConnectDisabled());
            return true;
        }

        if (args.length == 1) {
            boolean changed = !plugin.getConfigHandler().isDelay();

            plugin.getConfigHandler().setDelay(changed);
            plugin.sendMessage(sender, plugin.getMessageHandler().getDelaySwitch(changed));

            if (!changed) // disabled
                plugin.getBrokerManager().getFromType(NCBroker.BrokerType.DELAY).stopped();
            else
                plugin.getBrokerManager().tryBroker(NCBroker.BrokerType.DELAY);
            return true;
        } else if (args.length == 2) {
            int time;

            try {
                time = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                plugin.sendMessage(sender, "&cCould not parse number '" + args[1] + "'");
                return true;
            }

            plugin.getConfigHandler().setDelayTime(time);
            plugin.sendMessage(sender, plugin.getMessageHandler().getDelaySet(time));
        }

        return true;
    }

    @Override
    public int getArg() {
        return 0;
    }

    @Override
    public String getPermission() {
        return "nearchat.delay";
    }
}
