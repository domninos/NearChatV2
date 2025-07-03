package net.omni.nearChat.commands.subcommands;

import net.omc.database.OMCDatabase;
import net.omc.util.Flushable;
import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.commands.MainCommand;
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
                plugin.getDBConfigHandler().reload();
                plugin.getOMCConfig().reload();

                if (plugin.getDatabaseHandler().connect())
                    plugin.sendMessage(sender, plugin.getDBMessageHandler().getDBConnected());
                else
                    plugin.sendMessage(sender, plugin.getDBMessageHandler().getDBErrorConnectUnsuccessful());

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

                    OMCDatabase.Type type = OMCDatabase.Type.parseType(databaseArg);

                    if (type == null) {
                        plugin.sendMessage(sender, plugin.getDBMessageHandler().getDBSwitchArg());
                        return true;
                    }

                    String name = sender.getName();

                    if (!plugin.getPlayerManager().isSwitching(name)) {
                        plugin.getPlayerManager().setSwitching(name);
                        plugin.sendMessage(sender, plugin.getDBMessageHandler().getDBSwitchWarning());
                        return true;
                    }

                    // make sure there is caution since data may become unstable and should just stop the server and set database on config

                    try {
                        // reload before accessing config.
                        plugin.getPlayerManager().removeSwitching(name);

                        plugin.getDBConfigHandler().setDatabase(type);
                        plugin.getDBConfigHandler().reload();

                        plugin.sendMessage(sender, plugin.getDBMessageHandler().getDBTry());

                        if (sender instanceof Player)
                            plugin.sendConsole(plugin.getDBMessageHandler().getDBTry());

                        if (!type.isLoaded(plugin)) {
                            plugin.sendMessage(sender, plugin.getMessageHandler().getLibraryDownloading());

                            if (sender instanceof Player)
                                plugin.sendConsole(plugin.getMessageHandler().getLibraryDownloading());
                        }


                        // should be done async
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                            String toSend;

                            if (plugin.getDatabaseHandler().connect()) {
                                toSend = plugin.getDBMessageHandler().getDBConnected();

                                // load every online players' nearchat status
                                Bukkit.getOnlinePlayers().forEach(player ->
                                        plugin.getPlayerManager().loadEnabled(player));
                            } else
                                toSend = plugin.getDBMessageHandler().getDBErrorConnectUnsuccessful();

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
