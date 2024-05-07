package accordconsensus.shims;

import static accord.impl.IntKey.range;

import accord.api.Key;
import accord.api.MessageSink;
import accord.config.LocalConfig;
import accord.config.MutableLocalConfig;
import accord.coordinate.CoordinationAdapter;
import accord.impl.*;
import accord.local.Node;
import accord.local.NodeTimeService;
import accord.local.ShardDistributor;
import accord.messages.LocalRequest;
import accord.primitives.*;
import accord.topology.Shard;
import accord.topology.Topology;
import accord.utils.EpochFunction;
import accord.utils.async.AsyncChain;
import beguine.runtime.Arbitrary;

import accord.local.Node.Id;
import com.google.common.collect.ImmutableList;
import melina.CommandStore;
import melina.OverlayNetwork;
import melina.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.*;
import java.util.stream.Collectors;

import static accord.utils.async.AsyncChains.awaitUninterruptibly;

public class Cluster {
    public class Scheduler implements accord.api.Scheduler {
        // TODO: everything argh
        public class Scheduled implements accord.api.Scheduler.Scheduled {
            @Override
            public void cancel() {
            }
        }

        @Override
        public Scheduled recurring(Runnable run, long delay, TimeUnit units) {
            return new Scheduled();
        }

        @Override
        public Scheduled once(Runnable run, long delay, TimeUnit units) {
            return new Scheduled();
        }

        @Override
        public void now(Runnable run) {
            run.run();
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(Cluster.class);

    private AtomicLong walltime;
    private final Arbitrary arbitrary;
    private final Topology topology;
    private final LongSupplier nowSupplier;
    private final OverlayNetwork network;
    private final List<Node> nodes;


    public OverlayNetwork getNetwork() { return network; }

    public Cluster(Arbitrary a) {
        Range rng = range(0, 100);
        List<Id> ids = List.of(1,2,3).stream().map(Id::new).collect(Collectors.toList());
        Set<Id> fastpath = List.of(1,2).stream().map(Id::new).collect(Collectors.toSet());

        this.walltime = new AtomicLong(0L);
        this.arbitrary = a;
        this.topology = new Topology(1, new Shard(rng, ids, fastpath));
        this.nowSupplier = () -> walltime.longValue();

        this.nodes = new LinkedList<>();
        this.network = new OverlayNetwork(id -> Option.apply(this.nodes.stream().filter(n -> n.id().equals(id)).findFirst().orElse(null)));
        for (Node.Id id : ids) {
            this.nodes.add(newNode(id));
        }
    }

    private Node newNode(Id id) {
        Store melinaStore = new Store();
        MessageSink socket = network.dial(id);
        ConfigurationService configService = new ConfigurationService(EpochFunction.noop(), this.topology);
        LocalConfig localConfig = new MutableLocalConfig();

        Node node = new Node(id,
                socket,
                LocalRequest::process,
                configService,
                nowSupplier,
                NodeTimeService.unixWrapper(TimeUnit.MILLISECONDS, nowSupplier),
                () -> melinaStore,
                new ShardDistributor.EvenSplit(8, ignore -> new IntKey.Splitter()),
                new TestAgent(),
                new RandomSource(arbitrary),
                new Scheduler(),
                SizeOfIntersectionSorter.SUPPLIER,
                SimpleProgressLog::new,
                CommandStore.Factory::new,
                new CoordinationAdapter.DefaultFactory(),
                localConfig);

        awaitUninterruptibly(node.unsafeStart());
        node.onTopologyUpdate(topology, true);

        return node;
    }

    public void tick() {
        this.walltime.addAndGet(1);
    }

    public ImmutableList<Node> getNodes() {
        return ImmutableList.copyOf(nodes);
    }

    public Optional<Node> getNode(Id id) {
        return nodes.stream().filter(n -> n.id().equals(id)).findFirst();
    }

    public AsyncChain<accord.api.Result> read(Id id, Key k) {
        Keys keys = Keys.of(k);

        Node n = getNode(id).orElseThrow(() -> new RuntimeException("What node??"));

        TxnId txnid = n.nextTxnId(Txn.Kind.Read, Routable.Domain.Key);
        Txn txn = new Txn.InMemory(keys, new Store.Read(keys), new Store.Query(id));

        return n.coordinate(txnid, txn);
    }

    public AsyncChain<accord.api.Result> write(Id id, int k, int v) {
        IntKey.Raw key = new IntKey.Raw(k);
        Keys keys = Keys.of(key);

        Node n = getNode(id).orElseThrow(() -> new RuntimeException("What node??"));

        TxnId txnid = n.nextTxnId(Txn.Kind.Write, Routable.Domain.Key);
        Store.Data d = new Store.Data(key, v);
        Txn txn = new Txn.InMemory(keys, new Store.Read(keys), new Store.Query(id), new Store.Update(d));

        return n.coordinate(txnid, txn);
    }
}