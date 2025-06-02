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

        plugin.sendConsole("&aInitialized brokers. " + brokers);
    }


    // TODO optimize tasks. make sure to cancel tasks when no nearchat players online. re-run on join

    public void tryBrokers() {
        // check if running
        Set<NCBroker> cancelled = cancelBrokers();

        if (!cancelled.isEmpty()) {
            cancelled.forEach(broker -> {
                if (broker == null)
                    return;

                if (!broker.checkEmpty()) {
                    removeBroker(broker);

                    // then add
                    NCBroker copy = broker.copy();
                    addBroker(copy);
                    copy.init();
                    plugin.sendConsole("&aRe-initializing brokers..");
                } else
                    plugin.sendConsole(plugin.getMessageHandler().getBrokerEmptyCancel(broker.getBrokerName()));
            });
        } else {
            // empty cancelled, meaning on run
            brokers.forEach(this::tryBroker);
            plugin.sendConsole("on run");
        }
    }

    public boolean isNearbyRunning() {
        return getFromType(NCBroker.BrokerType.NEARBY).isRunning();
    }

    public boolean isDatabaseRunning() {
        return getFromType(NCBroker.BrokerType.DATABASE).isRunning();
    }

    public boolean isDelayRunning() {
        return getFromType(NCBroker.BrokerType.DELAY).isRunning();
    }

    public void addBroker(NCBroker broker) {
        brokers.add(broker);
    }

    public void removeBroker(NCBroker broker) {
        cancelBroker(broker);
        brokers.remove(broker);
        plugin.sendConsole("remove");
    }

    public void tryBroker(NCBroker broker) {
        if (broker == null)
            return;

        plugin.sendConsole("trying broker " + broker.getBrokerName());

        if (!broker.checkEmpty())
            broker.init();
        else
            plugin.sendConsole(plugin.getMessageHandler().getBrokerEmptyCancel(broker.getBrokerName()));
    }

    public void tryBroker(NCBroker.BrokerType type) {
        NCBroker fromType = getFromType(type);

        if (fromType != null)
            tryBroker(fromType);
    }

    public NCBroker getFromType(NCBroker.BrokerType type) {
        return brokers.stream().filter((broker) -> broker.getType() == type).findFirst().orElse(null);
    }

    public Set<NCBroker> cancelBrokers() {
        if (brokers.isEmpty())
            return brokers;

        Set<NCBroker> list = new HashSet<>();

        brokers.forEach(broker -> {
            if (broker != null && broker.isRunning()) {
                list.add(broker);
                broker.cancelBroker(true);

                if (broker instanceof Flushable)
                    ((Flushable) broker).flush();
            }
        });

        return list;
    }

    public void cancelBroker(NCBroker broker) {
        if (broker == null) return;

        if (!brokers.isEmpty())
            brokers.forEach(b -> {
                if (b.equals(broker)) {
                    b.cancelBroker();
                    plugin.sendConsole("cancelled " + broker.getBrokerName());
                }
            });
    }

    public void cancelBroker(NCBroker.BrokerType type) {
        NCBroker fromType = getFromType(type);

        if (fromType != null)
            cancelBroker(fromType);
    }

    @Override
    public void flush() {
        cancelBrokers().clear();
        brokers.clear();
    }
}
