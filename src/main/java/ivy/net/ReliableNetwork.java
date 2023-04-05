package ivy.net;

import ivy.Generator;
import ivy.functions.Action;
import ivy.sorts.IvySort;
import ivy.sorts.Sorts;
import org.javatuples.Pair;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

// TODO: need to be parameterized over nodeSort and msgSort.  How best to do so?
public class ReliableNetwork extends ivy.Protocol {
    private final Impl impl;
    private final Spec spec;

    public ReliableNetwork(Random r, int max_nodes, Consumer<Pair<Integer, Integer>> callback) {
        super(r);
        impl = new Impl(max_nodes, callback);
        spec = new Spec(max_nodes);

        Sorts.IvyInt nodeSort = mkInt("nodeSort", 0, max_nodes);
        Sorts.IvyInt msgSort = mkInt("msgSort");

        addConjecture("at-most-once-delivery", () -> spec.inFlight >= 0);
        addConjecture("eventual-delivery", () ->
            impl.routingTable.values().stream().allMatch(q -> q.isEmpty()) || spec.inFlight > 0
        );

        addAction(
                () -> {
                    int id = nodeSort.get();
                    int dst = nodeSort.get();
                    int msg = msgSort.get();
                    return new org.javatuples.Triplet<>(id, dst, msg);
                },
                t -> {
                    int id = t.getValue0();
                    int dst = t.getValue1();
                    int msg = t.getValue2();
                    impl.send(id, dst, msg);
                }
        );

        addAction(
                nodeSort::get,
                n -> impl.onRecv.apply(impl.routingTable.get(n).pop())
        );
    }

    public class Impl implements Network<Integer, Integer> {
        private final Action<Pair<Integer, Integer>, Void> onRecv;
        private final HashMap<Integer, ArrayDeque<Pair<Integer, Integer>>> routingTable;

        public Impl(int max_nodes, Consumer<Pair<Integer, Integer>> onRecv) {
            this.onRecv = new Action<>(
                    () -> {},
                    p -> { onRecv.accept(p); return null; },
                    (t) -> spec.inFlight--
            );

            routingTable = new HashMap<>();
        }

        @Override
        public void send(Integer self, Integer dst, Integer msg) {
            System.out.println(String.format("[%s] SEND %s", self, dst));
            routingTable.computeIfAbsent(dst, id -> new ArrayDeque<>()).add(new Pair<>(self, msg));
            spec.inFlight++;
        }

        //@Override
        //public void registerRecv(Integer self, BiConsumer<Integer, Integer> callback) {
        //    System.out.println(String.format("[%s] RECV %s", self));
        //    onRecv.put(self, callback.andThen((id, m) -> spec.inFlight--));
       // }
    }

    public class Spec {
        private int inFlight;

        public Spec(int max_nodes) {
            inFlight = 0;
        }
    }
}
