package net.omni.nearChat.managers;

import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.brokers.DatabaseBroker;
import net.omni.nearChat.brokers.DelayBroker;
import net.omni.nearChat.brokers.NCBroker;
import net.omni.nearChat.brokers.NearbyBroker;
import net.omni.nearChat.util.Flushable;

import java.util.HashSet;
import java.util.Set;

public class BrokerManager implements Flushable {
    private final Set<NCBroker> brokers = new HashSet<>();

    private final NearChatPlugin plugin;

    public BrokerManager(NearChatPlugin plugin) {
        this.plugin = plugin;

        addBroker(new DatabaseBroker(plugin));

        if (plugin.getConfigHandler().isDelay())
            addBroker(new DelayBroker(plugin));

        addBroker(new NearbyBroker(plugin));

        plugin.sendConsole("&aInitialized brokers: " + brokers);
    }

    // TODO fix copy#isRunning is returning false but started() is ran naman
    public void tryBrokers() {
        // check if running
        Set<NCBroker> cancelled = cancelBrokers();

        if (!cancelled.isEmpty()) {
            for (NCBroker broker : cancelled) {
                if (broker == null) continue;

                if (broker.checkEmpty()) {
                    plugin.sendConsole(plugin.getMessageHandler().getBrokerEmptyCancel(broker.getBrokerName()));
                    continue;
                }

                plugin.sendConsole("cancelled checking: " + broker.getBrokerName());
                plugin.sendConsole("cancelled running: " + broker.isRunning());

                removeBroker(broker);

                // then add
                NCBroker copy = broker.copy();

                tryBroker(copy, false);

                addBroker(copy);
            }

            plugin.sendConsole("&aRestarted brokers.");

            cancelled.clear(); // dump
            return;
        }

        // empty cancelled, meaning on run
        brokers.forEach(broker -> tryBroker(broker, true));
    }

    public boolean isNearbyRunning() {
        return getFromType(NCBroker.BrokerType.NEARBY).isRunning();
    }

    public boolean isDatabaseRunning() {
        return getFromType(NCBroker.BrokerType.DATABASE).isRunning();
    }

    public boolean isDelayRunning() {
        NCBroker fromType = getFromType(NCBroker.BrokerType.DELAY);

        if (fromType == null) // was disabled on run
            addBroker(new DelayBroker(plugin));

        return getFromType(NCBroker.BrokerType.DELAY).isRunning();
    }

    public void addBroker(NCBroker broker) {
        brokers.add(broker);
    }

    public void removeBroker(NCBroker broker) {
        brokers.remove(broker);
    }

    public void tryBroker(NCBroker broker, boolean onStart, boolean cancel) {
        if (broker == null)
            return;

        if (cancel && broker.isRestartable() && broker.isRunning())
            broker.cancelBroker(true);

        tryBroker(broker, onStart);

    }

    public void tryBroker(NCBroker broker, boolean onStart) {
        if (broker == null)
            return;

        if (!broker.checkEmpty())
            broker.init();
        else {
            if (!onStart)
                plugin.sendConsole(plugin.getMessageHandler().getBrokerEmptyCancel(broker.getBrokerName()));
        }
    }

    public void tryBroker(NCBroker.BrokerType type, boolean onStart) {
        NCBroker fromType = getFromType(type);

        if (fromType != null)
            tryBroker(fromType, onStart);
    }

    public void tryBroker(NCBroker.BrokerType type) {
        tryBroker(type, false);
    }

    public NCBroker getFromType(NCBroker.BrokerType type) {
        return brokers.stream().filter((broker) -> broker.getType() == type).findFirst().orElse(null);
    }

    public Set<NCBroker> cancelBrokers() {
        if (brokers.isEmpty())
            return brokers;

        Set<NCBroker> list = new HashSet<>();

        brokers.forEach(broker -> {
            if (broker == null)
                return;

            plugin.sendConsole("cancelBrokers " + broker.getBrokerName());
            plugin.sendConsole("cancelBrokers running = " + broker.isRunning());

            if (broker.isRestartable() && broker.isRunning()) {
                list.add(broker);
                broker.cancelBroker(true);

                if (broker instanceof Flushable)
                    ((Flushable) broker).flush();
            }
        });

        return list;
    }

    public void cancelBroker(NCBroker broker) {
        if (broker != null)
            broker.cancelBroker();
    }

    public void cancelBroker(NCBroker.BrokerType type) {
        NCBroker fromType = getFromType(type);

        if (fromType != null && fromType.isRunning())
            cancelBroker(fromType);
    }

    @Override
    public void flush() {
        cancelBrokers().clear();
        brokers.clear();
    }
}
