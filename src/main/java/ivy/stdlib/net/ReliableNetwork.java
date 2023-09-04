package ivy.stdlib.net;

import io.vavr.Tuple3;
import io.vavr.control.Either;
import ivy.exceptions.IvyExceptions.*;
import ivy.functions.Actions.Action1;
import ivy.sorts.Sorts;

import java.util.*;

public class ReliableNetwork<Msg> extends ivy.Protocol {
    public final Impl<Msg> impl;
    public final Spec<Msg> spec;

    public final Action1<Integer, Void> recv;


    public ReliableNetwork(Random r, Impl<Msg> i, Spec<Msg> s) {
        super(r);
        impl = i;
        spec = s;

        // TODO: should be visible outside
        Sorts.IvyInt nodeSort = sorts.mkInt("nodeSort", 0, 3);

        recv = Action1.from(
                id -> {
                    if (!impl.routingTable.containsKey(id)) {
                        return Either.left(new RetryGeneration());
                    }
                    if (impl.routingTable.get(id).size() == 0) {
                        return Either.left(new RetryGeneration());
                    }
                    return Either.right(null);
                },
                id -> {
                    Tuple3<Integer, Integer, Msg> t = impl.routingTable.get(id).pop();
                    t.apply(impl::recv);
                    return null;
                });
        addAction(recv.pipe(nodeSort::get));

        addConjecture("reliable-network-at-most-once-delivery", () -> spec.inFlight >= 0);
        addConjecture("reliable-network-eventual-delivery", () ->
                impl.routingTable.values().stream().allMatch(q -> q.isEmpty()) || spec.inFlight > 0);
    }

    public static abstract class Impl<Msg> implements Network<Integer, Msg> {
        private final HashMap<Integer, ArrayDeque<Tuple3<Integer, Integer, Msg>>> routingTable;

        public Impl() {
            routingTable = new HashMap<>();
        }

        @Override
        public Void send(Integer self, Integer dst, Msg msg) {
            System.out.println(String.format("[Net %3d] SEND %c to %d", self, msg, dst));
            routingTable.computeIfAbsent(dst, id -> new ArrayDeque<>()).add(new Tuple3<>(dst, self, msg));
            return null;
        }

        public abstract Void recv(Integer self, Integer src, Msg msg);
    }

    public static class Spec<Msg> {
        public int inFlight;

        public Spec() {
            inFlight = 0;
        }

        public Either<ActionException, Void> after_send(Void ret) {
            inFlight++;
            return Either.right(null);
        }

        public Either<ActionException, Void> after_recv(Void ret) {
            inFlight--;
            return Either.right(null);
        }
    }
}
