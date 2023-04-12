package net;

import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import ivy.Protocol;
import ivy.exceptions.IvyExceptions;
import ivy.functions.Actions.Action2;
import ivy.net.Network;
import ivy.net.ReliableNetwork;
import ivy.sorts.Sorts;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

public class SimpleChainRep {

    public class ChainReplication extends Protocol {
        Sorts.IvyInt nodeSort = sorts.mkInt("nodeSort", 0, 3);
        Sorts.IvyChar msgSort = sorts.mkPrintableAscii("msgSort");

        class Node {
            // TODO: we'll have to carefully think through mutable primitive types
            private int self;

            public final StringBuilder file;

            public Node(int self) {
                this.self = self;
                this.file = new StringBuilder();
            }

            public void onRecv(int msgSrc, char msg) {
                System.out.println(String.format("[node %3d] RECV %c from %d", self, msg, msgSrc));
                file.append(msg);
                if (self < (max_nodes) - 1) {
                    net.send(self, self + 1, msg);
                }
                System.out.println(String.format("[node %3d] File state: \"%s\"", self, file));
            }

            public void append(char msg) {
                System.out.println(String.format("[node %3d] Appending %c", self, msg));
                net.send(self, 0, msg);
            }
        }


        HashMap<Integer, Integer> self;
        HashMap<Integer, StringBuilder> file;
        HashMap<Integer, Function2<Integer, Character, Void>> onRecv;
        HashMap<Integer, Function1<Character, Void>> append;

        int max_nodes;

        private final Network<Integer, Character> net;

        public ChainReplication(int max_nodes) {
            super(new Random(42));

            this.max_nodes = max_nodes;

            ReliableNetwork.Impl<Character> net_impl = new ReliableNetwork.Impl<>() {
                @Override
                public Void recv(Integer self, Integer src, Character character) {
                    return onRecv.get(self).apply(src, character);
                }
            };
            ReliableNetwork.Spec net_spec = new ReliableNetwork.Spec();
            ReliableNetwork<Character> net = new ReliableNetwork<>(random, net_impl, net_spec);
            combine(net);

            Function2<Integer, Character, Void> f = (n, c) -> append.get(n).apply(c);
            Action2<Integer, Character, Void> appendAction = Action2.from(
                    (n, c) -> Either.right(null),
                    f,
                    net_spec::after_send);
            addAction(appendAction.pipe(() -> new Tuple2(nodeSort.get(), msgSort.get())));

            EventuallyConsistentSpec spec = new EventuallyConsistentSpec();

            this.net = net_impl;

            self = new HashMap<>();
            file = new HashMap<>();
            onRecv = new HashMap<>();
            append = new HashMap<>();

            for (int i = 0; i < max_nodes; i++) {
                Node n = new Node(i);
                self.put(i, n.self);
                file.put(i, n.file);
                onRecv.put(i, (s, c) -> { n.onRecv(s, c); return null; });
                append.put(i, c -> { n.append(c); return null; });
            }
        }

        public class EventuallyConsistentSpec {
            private boolean monotonicChain() {
                List<Integer> nodes = self.keySet().stream().collect(Collectors.toList());
                nodes.sort(Comparator.naturalOrder());

                String curr = file.get(nodes.get(0)).toString();
                for (int n : nodes) {
                    String prefix = file.get(n).toString();
                    if (!curr.startsWith(prefix)) {
                        return false;
                    }
                    prefix = curr;
                }
                return true;
            }
            public EventuallyConsistentSpec() {
                addConjecture("eventually-consistent-chain-prefix", this::monotonicChain);
            }
        }
    }


    @Test
    public void testActionGen() {
        ChainReplication c = new ChainReplication(3);
        for (int i = 0; i < 100; i++) {
            Either<IvyExceptions.ConjectureFailure, Void> res = c.takeAction();
            assert(res.isRight());
        }
    }
}
