package net.omni.nearChat;

import net.omni.nearChat.commands.MainCommand;
import net.omni.nearChat.commands.NearChatCommand;
import net.omni.nearChat.handlers.ConfigHandler;
import net.omni.nearChat.database.DatabaseHandler;
import net.omni.nearChat.handlers.MessageHandler;
import net.omni.nearChat.listeners.NCPlayerListener;
import net.omni.nearChat.managers.PlayerManager;
import net.omni.nearChat.util.NearChatConfig;
import net.omni.nearChat.util.brokers.DatabaseBroker;
import net.omni.nearChat.util.brokers.NearbyBroker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class NearChatPlugin extends JavaPlugin {

    private final List<MainCommand> mainCommands = new ArrayList<>();
    private final MessageHandler messageHandler;
    private NearChatConfig nearConfig;
    private NearChatConfig messageConfig;
    private final ConfigHandler configHandler;
    private final DatabaseHandler databaseHandler;
    private PlayerManager playerManager;

    public NearChatPlugin() {
        this.databaseHandler = new DatabaseHandler(this);
        this.messageHandler = new MessageHandler(this);
        this.configHandler = new ConfigHandler(this);
    }

    /*
    TODO:
        * Add /nearchat gui
          * Possibly create inventory handler.
        * Add database integration (set toggled on database for UUID) # why
        * Add option for flat-file database
     */

    @Override
    public void onEnable() {
        this.messageConfig = new NearChatConfig(this, "messages.yml", true);
        this.nearConfig = new NearChatConfig(this, "config.yml", true);

        configHandler.load();
        messageHandler.load();
        databaseHandler.initDatabase();

        this.playerManager = new PlayerManager(this);

        registerListeners();
        registerCommands();

        tryBrokers();

        messageHandler.sendEnabledMessage();
    }

    @Override
    public void onDisable() {
        configHandler.saveToConfig();
        messageHandler.saveToConfig();

        playerManager.saveToDatabase();

        flush();

        messageHandler.sendDisabledMessage();
    }

    public void error(Exception e) {
        error(e.getMessage());
    }

    public void error(String text) {
        getLogger().log(Level.SEVERE, "ERROR! Something went wrong: " + text);
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
        sender.sendMessage(translate(messageHandler.getPrefix() + msg));
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

    public void tryBrokers() {
        if (!getDatabaseHandler().isEnabled()) {
            sendConsole(getMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        sendConsole("&aInitializing broker..");

        new DatabaseBroker(this);
        new NearbyBroker(this);
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