package net.omni.nearChat.handlers;

import net.omc.config.ConfigAbstract;
import net.omc.config.value.ValueDef;
import net.omc.config.value.ValueType;
import net.omc.database.OMCDatabase;
import net.omni.nearChat.NearChatPlugin;

public class ConfigHandler extends ConfigAbstract {

    public ConfigHandler(NearChatPlugin plugin) {
        super(plugin);
    }

    @Override
    public void initialize() {
        flush();

        loadValues(plugin.getDBConfigHandler());

        // load messages to cache
        builder
                .fromConfig()
                .load("nearby-get-delay", ValueType.INT, 10)
                .load("database-save-delay", ValueType.INT, 6000)
                .load("near-block-radius", ValueType.INT, 30)

                .load("log-messages", ValueType.BOOLEAN, true)
                .load("delay-time", ValueType.INT, 3)
                .load("delay", ValueType.BOOLEAN, true)

                .fromPlugin()
                .load("plugin_name", ValueType.PLUGIN_NAME, "N/A")
                .load("plugin_version", ValueType.PLUGIN_VERSION, "N/A")
                .load("plugin_mc_version", ValueType.PLUGIN_API, "N/A")

                .save();
    }

    public void saveToConfig() {
        builder
                .fromConfig()
                .toSave("host", ValueType.STRING)
                .toSave("port", ValueType.INT)
                .toSave("user", ValueType.STRING)
                .toSave("password", ValueType.STRING)

                .toSave("nearby-get-delay", ValueType.INT)
                .toSave("database-save-delay", ValueType.INT)
                .toSave("near-block-radius", ValueType.INT)

                .toSave("log-messages", ValueType.BOOLEAN)

                .save();
    }

    public int getDelayTime() {
        return getInt("delay-time");
    }

    public boolean isDelay() {
        return getBool("delay");
    }

    public void setDelayTime(int delay_time) {
        builder.toSave("delay-time", ValueType.INT, ValueDef.from(delay_time)).save();
    }

    public void setDelay(boolean delay) {
        builder.toSave("delay", ValueType.BOOLEAN, ValueDef.from(delay)).save();
    }

    public OMCDatabase.Type getDatabaseType() {
        try {
            return OMCDatabase.Type.valueOf(getString("database-type").toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException e) {
            plugin.error(plugin.getDBMessageHandler().getDBSwitchArg());
            return null;
        }
    }

    public boolean isLogging() {
        return getBool("log-messages");
    }

    public int getNearbyGetDelay() {
        return getInt("nearby-get-delay");
    }

    public String getPluginName() {
        return getString("plugin_name");
    }

    public String getPluginVersion() {
        return getString("plugin_version");
    }

    public String getPluginMCVersion() {
        return getString("plugin_mc_version");
    }

    public int getDatabaseSaveDelay() {
        return getInt("database-save-delay");
    }

    public int getNearBlockRadius() {
        return getInt("near-block-radius");
    }
}
