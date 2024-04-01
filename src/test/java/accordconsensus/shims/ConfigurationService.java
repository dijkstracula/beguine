package accordconsensus.shims;

import accord.api.TestableConfigurationService;
import accord.primitives.Ranges;
import accord.topology.Topology;
import accord.utils.EpochFunction;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationService implements TestableConfigurationService {
    private EpochFunction<ConfigurationService> onFetchTopology;

    private final Map<Long, Topology> epochs;
    private final List<Listener> listeners;

    public ConfigurationService(EpochFunction<ConfigurationService> onFetchTopology, Topology initialTopology) {
        this.onFetchTopology = onFetchTopology;
        this.epochs = new HashMap<>();
        this.listeners = new ArrayList<>();

        epochs.put(0L, initialTopology);
    }

    @Override
    public void reportTopology(Topology topology) {
        // TODO: topology.epoch() > epochs.size() ??
        //assert(topology.epoch() == epochs.size());
        epochs.put(topology.epoch(), topology);

        for (Listener l : listeners) {
            l.onTopologyUpdate(topology, true);
        }
    }

    @Override
    public void registerListener(Listener listener) {
        listeners.add(listener);
    }

    @Override
    public Topology currentTopology() {
        return epochs.get(epochs.size() - 1);
    }

    @Nullable
    @Override
    public Topology getTopologyForEpoch(long epoch) {
        return epochs.get(epoch);
    }

    @Override
    public void fetchTopologyForEpoch(long epoch) {
        if (epochs.containsKey(epoch)) {
            return;
        }
        onFetchTopology.apply(epoch, this);
    }

    @Override
    public void acknowledgeEpoch(EpochReady ready, boolean startSync) {
        
    }

    @Override
    public void reportEpochClosed(Ranges ranges, long epoch) {

    }

    @Override
    public void reportEpochRedundant(Ranges ranges, long epoch) {

    }
}
