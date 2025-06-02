package net.omni.nearChat.brokers;

import net.omni.nearChat.NearChatPlugin;
import org.bukkit.Bukkit;

public abstract class NCBroker {
    public final NearChatPlugin plugin;
    private final BrokerType brokerType;
    private final String broker_name;

    private boolean started;

    protected int taskId = -1;

    public NCBroker(NearChatPlugin plugin, BrokerType type) {
        this.plugin = plugin;
        this.brokerType = type;
        this.broker_name = type.getLabel();
    }

    public abstract void init();

    public void cancelBroker() {
        cancelBroker(false);
    }

    public void cancelBroker(boolean log) {
        cancelBrokerError(log, null);
    }

    public void cancelBrokerError(boolean log, Throwable throwable) {
        if (!isRunning())
            return;

        Bukkit.getScheduler().cancelTask(taskId);

        if (throwable != null)
            plugin.error("Cancelled " + getBrokerName() + " broker.", throwable);
        else if (log)
            plugin.sendConsole("Cancelled " + getBrokerName() + " broker.");
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public abstract void brokerRun();

    public abstract boolean checkEmpty();

    public boolean isRunning() {
        try {
            return started && taskId != -1 && Bukkit.getScheduler().isCurrentlyRunning(taskId);
        } catch (IllegalStateException e) {
            return false;
        }
    }

    public void starting() {
        this.started = true;
    }

    public String getBrokerName() {
        return this.broker_name;
    }

    public BrokerType getType() {
        return brokerType;
    }

    public enum BrokerType {
        DATABASE("database"), DELAY("delay"), NEARBY("nearby");

        private final String label;

        BrokerType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public NCBroker copy() {
        NCBroker current = this;

        return new NCBroker(current.plugin, current.brokerType) {
            @Override
            public void init() {
                current.init();
            }

            @Override
            public void brokerRun() {
                current.brokerRun();
            }

            @Override
            public boolean checkEmpty() {
                return current.checkEmpty();
            }
        };
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NCBroker && ((NCBroker) obj).getBrokerName().equals(getBrokerName());
    }

    @Override
    public String toString() {
        return this.broker_name;
    }
}
