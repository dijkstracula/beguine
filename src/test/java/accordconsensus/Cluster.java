package accordconsensus;

import static accord.impl.IntKey.keys;
import static accord.impl.IntKey.range;

import accord.api.MessageSink;
import accord.config.LocalConfig;
import accord.config.MutableLocalConfig;
import accord.coordinate.CoordinationAdapter;
import accord.impl.*;
import accord.impl.mock.MockConfigurationService;
import accord.local.Node;
import accord.local.NodeTimeService;
import accord.local.ShardDistributor;
import accord.messages.LocalRequest;
import accord.primitives.Range;
import accord.topology.Shard;
import accord.topology.Topology;
import accord.utils.EpochFunction;
import accord.utils.ThreadPoolScheduler;
import accordconsensus.shims.ConfigurationService;
import accordconsensus.shims.OverlayNetwork;
import beguine.runtime.Arbitrary;

import accord.local.Node.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Collectors;

import static accord.impl.PrefixedIntHashKey.ranges;
import static accord.utils.Utils.toArray;
import static accord.utils.async.AsyncChains.awaitUninterruptibly;

public class Cluster {
    private static final Logger logger = LoggerFactory.getLogger(Cluster.class);

    private final Arbitrary arbitrary;

    private final Topology topology;

    private final LongSupplier nowSupplier;

    private final OverlayNetwork network;


    public Cluster(Arbitrary a) {
        Range rng = range(0, 100);
        List<Id> ids = List.of(0,1,2).stream().map(Id::new).collect(Collectors.toList());
        Set<Id> fastpath = List.of(0,1).stream().map(Id::new).collect(Collectors.toSet());

        this.arbitrary = a;
        this.topology = new Topology(1, new Shard(rng, ids, fastpath));
        this.nowSupplier = () -> 42L;
        this.network = new OverlayNetwork();


        List<Node> nodes = ids.stream().map(id -> newNode(id)).collect(Collectors.toList());
    }

    public Node newNode(Id id) {
        Store store = new Store();
        MessageSink socket = network.createSink(id);
        ConfigurationService configService = new ConfigurationService(EpochFunction.noop(), this.topology);
        LocalConfig localConfig = new MutableLocalConfig();

        Node node = new Node(id,
                socket,
                LocalRequest::process,
                configService,
                nowSupplier,
                NodeTimeService.unixWrapper(TimeUnit.MILLISECONDS, nowSupplier),
                () -> store,
                new ShardDistributor.EvenSplit(8, ignore -> new IntKey.Splitter()),
                new TestAgent(),
                null /* yolo */,
                new ThreadPoolScheduler(),
                SizeOfIntersectionSorter.SUPPLIER,
                SimpleProgressLog::new,
                InMemoryCommandStores.SingleThread::new,
                new CoordinationAdapter.DefaultFactory(),
                localConfig);

        awaitUninterruptibly(node.unsafeStart());
        node.onTopologyUpdate(topology, true);

        return node;
    }
}