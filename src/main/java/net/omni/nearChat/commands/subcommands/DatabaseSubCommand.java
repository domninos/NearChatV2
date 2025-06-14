package net.omni.nearChat.commands.subcommands;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.commands.MainCommand;
import net.omni.nearChat.database.NearChatDatabase;
import net.omni.nearChat.util.Flushable;
import org.bukkit.command.CommandSender;

import java.util.HashSet;
import java.util.Set;

public class DatabaseSubCommand extends SubCommand implements Flushable {
    private final Set<String> switching = new HashSet<>();

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
        if (args.length == 1) {
            try {
                // reload before accessing config.
                plugin.getNearConfig().reload();

                if (plugin.getDatabaseHandler().connect())
                    plugin.sendMessage(sender, plugin.getMessageHandler().getDBConnected());
                else
                    plugin.sendMessage(sender, plugin.getMessageHandler().getDBErrorConnectUnsuccessful());

                return true;
            } catch (Exception e) {
                plugin.error("Something went wrong loading database: ", e);
            }
        } else {
            if (args[1].equalsIgnoreCase("switch")) {
                if (args.length == 2) {
                    if (!(sender.hasPermission("nearchat.db.switch"))) {
                        plugin.sendMessage(sender, plugin.getMessageHandler().getNoPermission());
                        return true;
                    }

                    mainCommand.sendHelp(sender);
                    return true;
                } else if (args.length == 3) {
                    if (!switching.contains(sender.getName())) {
                        switching.add(sender.getName());
                        plugin.sendMessage(sender, plugin.getMessageHandler().getDBSwitchWarning());
                        return true;
                    }

                    String databaseArg = args[2];

                    // make sure there is caution since data may become unstable and should just stop the server and set database on config

                    NearChatDatabase.Type type = NearChatDatabase.Type.parseType(databaseArg);

                    if (type == null) {
                        plugin.sendMessage(sender, plugin.getMessageHandler().getDBSwitchArg());
                        return true;
                    }

                    try {
                        // reload before accessing config.
                        switching.remove(sender.getName());

                        plugin.getConfigHandler().setDatabase(type);
                        plugin.getNearConfig().reload();

                        if (plugin.getDatabaseHandler().connect()) {
                            plugin.sendMessage(sender, plugin.getMessageHandler().getDBConnected());
                        } else
                            plugin.sendMessage(sender, plugin.getMessageHandler().getDBErrorConnectUnsuccessful());

                        return true;
                    } catch (Exception e) {
                        plugin.error("Something went wrong loading database: ", e);
                    }

                }
            }
        }

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

    @Override
    public void flush() {
        this.switching.clear();
    }
}
