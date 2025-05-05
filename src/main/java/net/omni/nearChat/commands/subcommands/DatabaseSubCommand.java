package net.omni.nearChat.commands.subcommands;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.commands.MainCommand;
import org.bukkit.command.CommandSender;

public class DatabaseSubCommand extends SubCommand {
    public DatabaseSubCommand(NearChatPlugin plugin, MainCommand mainCommand) {
        super(plugin, mainCommand);
    }

    @Override
    public String getCommand() {
        return "database";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"db", "data"};
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        try {
            // reload before accessing config.
            plugin.getNearConfig().reload();

            if (plugin.getDatabaseHandler().connectConfig()) {
                plugin.sendMessage(sender, plugin.getMessageHandler().getDBConnected());
                return true;
            }
        } catch (Exception e) {
            plugin.sendMessage(sender, "&cSomething went wrong loading database: " + e.getMessage());
        }

        // TODO: /nearchat database set <host,port,user,password> <value>

        return true;
    }

    @Override
    public int getArg() {
        return 0;
    }

    @Override
    public String getPermission() {
        return "nearchat.db";
    }
}
