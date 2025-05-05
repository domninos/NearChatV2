package net.omni.nearChat;

import net.omni.nearChat.commands.MainCommand;
import net.omni.nearChat.commands.NearChatCommand;
import net.omni.nearChat.handlers.ConfigHandler;
import net.omni.nearChat.handlers.DatabaseHandler;
import net.omni.nearChat.handlers.MessageHandler;
import net.omni.nearChat.listeners.NCPlayerListener;
import net.omni.nearChat.managers.PlayerManager;
import net.omni.nearChat.util.DatabaseBroker;
import net.omni.nearChat.util.NearChatConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class NearChatPlugin extends JavaPlugin {

    private final List<MainCommand> mainCommands = new ArrayList<>();
    private MessageHandler messageHandler;
    private NearChatConfig nearConfig;
    private NearChatConfig messageConfig;
    private ConfigHandler configHandler;
    private DatabaseHandler databaseHandler;
    private PlayerManager playerManager;

    /*
    TODO:
        * Add /nearchat gui
          * Possibly create inventory handler.
        * Add plugin worker (for running tasks for every player that has nearchat enabled to check nearby players.) # why ? just check for chat send
        * Add database integration (set toggled on database for UUID) # why
     */

    @Override
    public void onEnable() {
        this.nearConfig = new NearChatConfig(this, "config.yml", true);
        this.messageConfig = new NearChatConfig(this, "messages.yml", true);

        this.databaseHandler = new DatabaseHandler(this);
        this.configHandler = new ConfigHandler(this);
        this.messageHandler = new MessageHandler(this);

        configHandler.load();
        messageHandler.load();
        databaseHandler.initDatabase();

        this.playerManager = new PlayerManager(this);

        registerListeners();
        registerCommands();
        registerBroker();

        sendConsole("&aSuccessfully enabled "
                + getDescription().getFullName() + " [" + getDescription().getAPIVersion() + "]");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        configHandler.saveToConfig();
        messageHandler.saveToConfig();

        playerManager.saveToDatabase();

        flush();

        sendConsole("&cSuccessfully disabled " + getDescription().getFullName() + " [" + getDescription().getAPIVersion() + "]");
    }

    public void error(Exception e) {
        error(e.getMessage());
    }

    public void error(String text) {
        getLogger().log(Level.SEVERE, translate("&cERROR! Something went wrong: " + text));
    }

    public void sendConsole(String text) {
        sendMessage(Bukkit.getConsoleSender(), text);
    }

    public NearChatConfig getNearConfig() {
        return this.nearConfig;
    }

    public NearChatConfig getMessageConfig() {
        return messageConfig;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public DatabaseHandler getDatabaseHandler() {
        return databaseHandler;
    }

    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public List<MainCommand> getCommands() {
        return mainCommands;
    }

    public void sendMessage(CommandSender sender, String msg) {
        sender.sendMessage(translate("&f[&6Near&eChat&f] &7" + msg));
    }

    public String translate(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private void registerListeners() {
        sendConsole("&aInitializing listeners..");

        Bukkit.getPluginManager().registerEvents(new NCPlayerListener(this), this);
    }

    private void registerCommands() { // TODO: if ever add more commands
        sendConsole("&aInitializing commands..");

        new NearChatCommand(this).register();
    }

    private void registerBroker() {
        sendConsole("&aInitializing broker..");

        new DatabaseBroker(this);
    }

    private void flush() {
        nearConfig.save();
        messageConfig.save();

        messageHandler.flush();
        databaseHandler.closeDatabase();

        if (!mainCommands.isEmpty()) {
            mainCommands.stream()
                    .filter(mainCommand -> !mainCommand.getSubCommands().isEmpty())
                    .forEach(MainCommand::flush);

            mainCommands.clear();
        }

        playerManager.flush();
    }
}