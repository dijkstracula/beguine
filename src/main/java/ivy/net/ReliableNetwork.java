package ivy.net;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Consumer;

public class ReliableNetwork<Id extends Comparable<Id>, M> extends ivy.Protocol<ReliableNetwork.Impl> {
    private final Impl impl;

    public ReliableNetwork(Random r) {
        super(r);
        impl = new Impl();
    }

    public class Impl implements Network<Id, M> {
        private final HashMap<Id, Consumer<M>> handlers;
        private final HashMap<Id, ArrayDeque<M>> routingTable;

        public Impl() {
            handlers = new HashMap<>();
            routingTable = new HashMap<>();
        }

        @Override
        public void send(Id self, Id dst, M msg) {
            routingTable.computeIfAbsent(dst, id -> new ArrayDeque<>()).add(msg);
        }

        @Override
        public void registerRecv(Id self, Consumer<M> callback) {
            handlers.put(self, callback);
        }
    }
}
