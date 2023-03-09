import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import accord.local.Node;
import accord.topology.Shard;
import accord.topology.Topology;

/**
 * A simplified TopologyFactory from the one in the Maelstrom test harness, specialized to how the Ivy runtime and
 * executable spec does things at the moment.
 *
 * Simplifications:
 * - Node IDs on [0..nodes)
 * - Keys and values are "unsigned" ints (i.e. ranges are on [0..INT_MAX)), reflecting Ivy's instantiation of
 *   uninterpreted types as nats
 * - No sharding (each node is a full replica; conversely, there is only one shard comprising all nodes)
 * - Node `i`'s fast path electorates on [i, i + `electorateSize` % nodes)
 */
public class TopologyBuilder {
    Optional<Integer> nodes;
    Optional<Integer> fastPathElectorateSize;

    public TopologyBuilder() {
        nodes = Optional.empty();
        fastPathElectorateSize = Optional.empty();
    }

    public TopologyBuilder withMaxNodes(int n) {
        nodes = Optional.of(n);
        if (fastPathElectorateSize.isEmpty()) {
            fastPathElectorateSize = Optional.of(n);
        }
        return this;
    }

    public TopologyBuilder withFastPathElectorateSize(int n) {
       fastPathElectorateSize = Optional.of(n);
       return this;
    }

    public Topology build() {
        int nodes = this.nodes.orElseThrow(() -> new RuntimeException("Nodes unset"));
        int fpESz = this.fastPathElectorateSize.orElseThrow(() -> new RuntimeException("Fast path electorate size unset"));

        List<Node.Id> slowpathElectorate = IntStream.range(0, nodes)
                .mapToObj(Node.Id::new)
                .collect(Collectors.toList());
        Set<Node.Id> fastPathElectorate = IntStream.range(0, fpESz)
                .mapToObj(Node.Id::new)
                .collect(Collectors.toSet());

        Shard s = new Shard(IvyKey.FULL_RANGE, slowpathElectorate, fastPathElectorate);
        return new Topology(1L, new Shard[]{s});
    }
}
