package net.omni.nearChat.commands.subcommands;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.commands.MainCommand;
import org.bukkit.command.CommandSender;

public class ReloadSubCommand extends SubCommand {
    public ReloadSubCommand(NearChatPlugin plugin, MainCommand mainCommand) {
        super(plugin, mainCommand);
    }

    @Override
    public String getCommand() {
        return "reload";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"rl"};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        plugin.getNearConfig().reload();
        plugin.getConfigHandler().load();

        plugin.getMessageConfig().reload();
        plugin.getMessageHandler().load();

        plugin.sendMessage(sender, "Reloaded config and messages.yml"); // TODO messages.yml
        return true;
    }

    @Override
    public int getArg() {
        return 0;
    }

    @Override
    public String getPermission() {
        return "nearchat.reload";
    }
}
