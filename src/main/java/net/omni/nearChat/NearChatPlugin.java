package net.omni.nearChat;

import net.omni.nearChat.commands.NearChatCommand;
import net.omni.nearChat.handlers.DatabaseHandler;
import net.omni.nearChat.handlers.MessageHandler;
import net.omni.nearChat.util.NearChatConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class NearChatPlugin extends JavaPlugin {

    private MessageHandler messageHandler;
    private NearChatConfig nearConfig;
    private NearChatConfig messageConfig;
    private DatabaseHandler databaseHandler;

    @Override
    public void onEnable() {
        this.nearConfig = new NearChatConfig(this, "config.yml", true);
        this.messageConfig = new NearChatConfig(this, "messages.yml", true);

        this.databaseHandler = new DatabaseHandler(this);
        this.messageHandler = new MessageHandler(this);

        databaseHandler.initDatabase();
        messageHandler.load();
        // Plugin startup logic

        registerListeners();
        registerCommands();

        sendConsole("&aSuccessfully enabled "
                + getDescription().getFullName() + " [" + getDescription().getAPIVersion() + "]");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        // TODO: configHandler.saveToConfig()
//        messageHandler.saveToConfig();

        nearConfig.save();
        messageConfig.save();

        messageHandler.flush();
        databaseHandler.closeDatabase();

        sendConsole("&cSuccessfully disabled "
                + getDescription().getFullName() + " [" + getDescription().getAPIVersion() + "]");
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

    public void sendMessage(CommandSender sender, String msg) {
        sender.sendMessage(translate("&f[&6Near&eChat&f] &7" + msg));
    }

    public String translate(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private void registerListeners() {
        // TODO
        sendConsole("&aInitializing listeners..");

//        Bukkit.getPluginManager().registereven
    }

    private void registerCommands() {
        // TODO

        new NearChatCommand(this).register();
        sendConsole("&aCommands initialized.");
    }
}