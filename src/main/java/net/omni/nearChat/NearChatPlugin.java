package net.omni.nearChat;

import net.omni.nearChat.commands.MainCommand;
import net.omni.nearChat.commands.NearChatCommand;
import net.omni.nearChat.handlers.ConfigHandler;
import net.omni.nearChat.handlers.DatabaseHandler;
import net.omni.nearChat.handlers.MessageHandler;
import net.omni.nearChat.listeners.NCPlayerListener;
import net.omni.nearChat.managers.BrokerManager;
import net.omni.nearChat.managers.HikariManager;
import net.omni.nearChat.managers.PAPIManager;
import net.omni.nearChat.managers.PlayerManager;
import net.omni.nearChat.util.Flushable;
import net.omni.nearChat.util.MainUtil;
import net.omni.nearChat.util.NearChatConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class NearChatPlugin extends JavaPlugin implements Flushable {

    private final List<MainCommand> mainCommands = new ArrayList<>();

    private NearChatConfig nearConfig;
    private NearChatConfig messageConfig;

    private final ConfigHandler configHandler;
    private final MessageHandler messageHandler;
    private final DatabaseHandler databaseHandler;

    private PlayerManager playerManager;
    private HikariManager hikariManager;
    private BrokerManager brokerManager;

    public NearChatPlugin() {
        this.databaseHandler = new DatabaseHandler(this);
        this.messageHandler = new MessageHandler(this);
        this.configHandler = new ConfigHandler(this);
    }

    /*
    TODO:
        *
        * RETEST ALL DATABASE
        *
        * database to implement: sqlite, mongodb, mysql
        *
        *
        * load the only library when switching databases
        * block censored/blacklisted words (research for more accuracy)
        * add admin command to inspect nearchat of player (send all messages sent from and to player)
        *
        *
        * fetch for updates (TRY) on load
        *
        * `broker_cancel` on disable
        *
        * Make all messages on messages.yml.
        * Redo messages (make sure to replace on MessageHandler.java)
        *
        *
        *
        * make messages on MessageHandler follow polymorphism (or store objects in map) [not sure if efficient or necessary]
        * Add /nearchat gui
          * Possibly create inventory handler. (necessary ?) [FUTURE]
     */

    @Override
    public void onEnable() {
        MainUtil.loadLibraries(this);

        this.messageConfig = new NearChatConfig(this, "messages.yml", true);
        this.nearConfig = new NearChatConfig(this, "config.yml", true);

        configHandler.load();
        messageHandler.load();

        this.brokerManager = new BrokerManager(this);

        this.hikariManager = new HikariManager(this);

        databaseHandler.connect();

        this.playerManager = new PlayerManager(this);

        registerListeners();
        registerCommands();

        checkPapi();

        addHook();

        messageHandler.sendEnabledMessage();
    }

    @Override
    public void onDisable() {
        configHandler.saveToConfig();
        messageHandler.saveToConfig();

        if (playerManager != null)
            playerManager.saveMap(false);

        hikariManager.close();

        messageHandler.sendDisabledMessage();

        flush();
    }

    public void error(String message, Throwable throwable) {
        getLogger().log(Level.SEVERE, translate(message), throwable);
        sendConsole(message + " " + throwable.getMessage());
    }

    public void error(String text) {
        sendConsole("&cERROR! Something went wrong: " + text);
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

    public HikariManager getHikariManager() {
        return hikariManager;
    }

    public BrokerManager getBrokerManager() {
        return brokerManager;
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

        brokerManager.tryBrokers();
    }

    @Override
    public void flush() {
        nearConfig.save();
        messageConfig.save();

        databaseHandler.closeDatabase();

        if (!mainCommands.isEmpty()) {
            mainCommands.stream()
                    .filter(mainCommand -> !mainCommand.getSubCommands().isEmpty())
                    .forEach(MainCommand::flush);

            mainCommands.clear();
        }

        if (playerManager != null)
            playerManager.flush();

        messageHandler.flush();

        brokerManager.flush();

        Bukkit.getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);
    }

    public void addHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (DatabaseHandler.ADAPTER != null && DatabaseHandler.ADAPTER.getDatabase() != null)
                DatabaseHandler.ADAPTER.lastSaveMap();
        }));
    }

    public void checkPapi() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            PAPIManager papiManager = new PAPIManager(this);

            papiManager.checkPapi();
        }
    }
}