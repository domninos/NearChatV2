package net.omni.nearChat.commands.subcommands;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.commands.MainCommand;
import net.omni.nearChat.database.NearChatDatabase;
import net.omni.nearChat.util.Flushable;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DatabaseSubCommand extends SubCommand implements Flushable {

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
                    String databaseArg = args[2];

                    NearChatDatabase.Type type = NearChatDatabase.Type.parseType(databaseArg);

                    if (type == null) {
                        plugin.sendMessage(sender, plugin.getMessageHandler().getDBSwitchArg());
                        return true;
                    }

                    String name = sender.getName();

                    if (!plugin.getPlayerManager().isSwitching(name)) {
                        plugin.getPlayerManager().setSwitching(name);
                        plugin.sendMessage(sender, plugin.getMessageHandler().getDBSwitchWarning());
                        return true;
                    }

                    // make sure there is caution since data may become unstable and should just stop the server and set database on config

                    try {
                        // reload before accessing config.
                        plugin.getPlayerManager().removeSwitching(name);

                        plugin.getConfigHandler().setDatabase(type);
                        plugin.getNearConfig().reload();

                        plugin.sendMessage(sender, plugin.getMessageHandler().getDBTry());

                        if (sender instanceof Player)
                            plugin.sendConsole(plugin.getMessageHandler().getDBTry());

                        if (!type.isLoaded(plugin)) {
                            plugin.sendMessage(sender, plugin.getMessageHandler().getLibraryDownloading());

                            if (sender instanceof Player)
                                plugin.sendConsole(plugin.getMessageHandler().getLibraryDownloading());
                        }


                        // should be done async
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                            String toSend;

                            if (plugin.getDatabaseHandler().connect()) {
                                toSend = plugin.getMessageHandler().getDBConnected();

                                // load every online players' nearchat status
                                Bukkit.getOnlinePlayers().forEach(player ->
                                        plugin.getPlayerManager().loadEnabled(player));
                            } else
                                toSend = plugin.getMessageHandler().getDBErrorConnectUnsuccessful();

                            plugin.sendMessage(sender, toSend);

                            if (sender instanceof Player) // also send message to console
                                plugin.sendConsole(toSend);
                        });

                        return true;
                    } catch (Exception e) {
                        plugin.error("Something went wrong loading database: ", e);
                    }

                }
            } else {
                mainCommand.sendHelp(sender);
                return true;
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
    }
}
