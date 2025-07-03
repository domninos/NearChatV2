package net.omni.nearChat;

import net.omc.OMCApi;
import net.omc.OMCPlugin;
import net.omc.config.OMCConfig;
import net.omc.handlers.LibraryHandler;
import net.omc.handlers.OMCDatabaseHandler;
import net.omc.util.Flushable;
import net.omni.nearChat.commands.MainCommand;
import net.omni.nearChat.commands.NearChatCommand;
import net.omni.nearChat.handlers.ConfigHandler;
import net.omni.nearChat.handlers.DatabaseHandler;
import net.omni.nearChat.handlers.MessageHandler;
import net.omni.nearChat.handlers.VersionHandler;
import net.omni.nearChat.listeners.NCPlayerListener;
import net.omni.nearChat.managers.BrokerManager;
import net.omni.nearChat.managers.GitManager;
import net.omni.nearChat.managers.PAPIManager;
import net.omni.nearChat.managers.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.List;

public final class NearChatPlugin extends OMCPlugin implements Flushable {

    private final List<MainCommand> mainCommands = new ArrayList<>();

    private OMCConfig nearConfig;
    private OMCConfig messageConfig;

    private final ConfigHandler configHandler;
    private final MessageHandler messageHandler;
    private final OMCDatabaseHandler databaseHandler;
    private final VersionHandler versionHandler;

    private PlayerManager playerManager;
    private BrokerManager brokerManager;
    private GitManager gitManager;


    private LibraryHandler libraryHandler;

    public NearChatPlugin() {
        this.databaseHandler = new DatabaseHandler(this);
        this.messageHandler = new MessageHandler(this);
        this.configHandler = new ConfigHandler(this);
        this.versionHandler = new VersionHandler(this);
    }

    /*
    TODO:
        *
        * RETEST ALL DATABASE
        *
        * database to implement: mysql, mariadb, mongodb
        *
        * check for sqlite driver. if not found, revert to flat-file
        *  - make sure to include flat-file is not encouraged as it is resource intensive
        *
        *
        * MAKE IMPLEMENTATIONS OF:
        *  - chat plugins
        *  - censored/blacklisted plugins/words
        *  - chat logger
        *  - [show] items plugin things
        *
        *
        *
        *
        *
        *
        * block censored/blacklisted words (research for more accuracy)
        * add admin command to inspect nearchat of player (get all messages sent from and to player)
        *
        *
        *
        * Make all messages on messages.yml.
        * Redo messages (make sure to replace on MessageHandler.java)
        *
        *
        *
        *
        * CLEANUPPPPPPPPPPPPPPPPPPPP
        *
        *
        *
        * make messages on MessageHandler follow polymorphism (or store objects in map) [not sure if efficient or necessary]
        * Add /nearchat gui
          * Possibly create inventory handler. (necessary ?) [FUTURE]
     */

    @Override
    public void onEnable() {
        this.messageConfig = new OMCConfig(this, "messages.yml", true);
        this.nearConfig = new OMCConfig(this, "config.yml", true);

        getDBConfigHandler().load(this.nearConfig);
        getDBConfigHandler().initialize();

        configHandler.load(nearConfig);
        configHandler.initialize();

        getDBMessageHandler().load(this.messageConfig);
        getDBMessageHandler().initialize();

        messageHandler.load(messageConfig);
        messageHandler.initialize();

        this.gitManager = new GitManager(this);

        versionHandler.checkForUpdates();

        this.libraryHandler = OMCApi.getInstance().getLibraryHandler(this);
        libraryHandler.setLibraryPath("net{}omni{}nearChat{}libs");
        libraryHandler.ensureMainLibraries();


        this.brokerManager = new BrokerManager(this);

        getHikariManager();

        Bukkit.getScheduler().runTaskAsynchronously(this, databaseHandler::connect);

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

        getHikariManager().close();

        messageHandler.sendDisabledMessage();

        libraryHandler.stopExecutor();

        flush();
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }


    public VersionHandler getVersionHandler() {
        return versionHandler;
    }


    public BrokerManager getBrokerManager() {
        return brokerManager;
    }

    public GitManager getGitManager() {
        return gitManager;
    }

    public List<MainCommand> getCommands() {
        return mainCommands;
    }

    @Override
    public void registerListeners() {
        sendConsole("&aInitializing listeners..");

        Bukkit.getPluginManager().registerEvents(new NCPlayerListener(this), this);
    }

    @Override
    public void registerCommands() { // TODO: if ever add more commands
        sendConsole("&aInitializing commands..");

        new NearChatCommand(this).register();
    }

    @Override
    public String getPrefix() {
        return getMessageHandler().getPrefix();
    }

    @Override
    public String getNetworkPrefix() {
        return "OMCN";
    }

    public void tryBrokers() {
        if (!getDatabaseHandler().isEnabled()) {
            sendConsole(getDBMessageHandler().getDBErrorConnectDisabled());
            return;
        }

        brokerManager.tryBrokers();
    }

    @Override
    public OMCConfig getOMCConfig() {
        return this.nearConfig;
    }

    public OMCConfig getMessageConfig() {
        return messageConfig;
    }

    @Override
    public OMCDatabaseHandler getDatabaseHandler() {
        return this.databaseHandler;
    }

    @Override
    public void stopLibraryExecutor() {
        getLibraryHandler().stopExecutor();
    }

    public OMCPlugin asOMC() {
        return this;
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

        brokerManager.flush();

        configHandler.flush();
        messageHandler.flush();

        Bukkit.getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);
    }

    public void addHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (DatabaseHandler.ADAPTER != null)
                DatabaseHandler.ADAPTER.lastSaveMap();
        }));
    }

    public void checkPapi() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            PAPIManager papiManager = new PAPIManager(this);

            papiManager.checkPapi();
        }
    }

    // TODO support HolographicDisplaysAPI (only send holograms to players nearby and has nearchat)
}