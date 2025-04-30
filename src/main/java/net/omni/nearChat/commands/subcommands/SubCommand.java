package net.omni.nearChat.commands.subcommands;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.commands.MainCommand;
import org.bukkit.command.CommandSender;

public abstract class SubCommand {

    protected final NearChatPlugin plugin;
    protected final MainCommand mainCommand;

    public SubCommand(NearChatPlugin plugin, MainCommand mainCommand) {
        this.plugin = plugin;
        this.mainCommand = mainCommand;
    }

    public abstract String getCommand();

    public abstract boolean execute(CommandSender sender, String[] args);

    public abstract int getArg();
}
