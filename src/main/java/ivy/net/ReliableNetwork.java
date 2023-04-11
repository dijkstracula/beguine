package ivy.net;

import io.vavr.Function0;
import io.vavr.Function3;
import io.vavr.Tuple3;
import io.vavr.control.Either;
import ivy.exceptions.IvyExceptions;
import ivy.exceptions.IvyExceptions.*;
import ivy.functions.Actions.Action0;
import ivy.functions.Actions.Action1;
import ivy.functions.Actions.Action3;
import ivy.sorts.Sorts;

import java.util.*;
import java.util.stream.Collectors;

public class ReliableNetwork<Msg> extends ivy.Protocol {
    public final Impl<Msg> impl;
    public final Spec<Msg> spec;

    public final Action3<Integer, Integer, Msg, Void> send;
    public final Action1<Integer, Void> recv;


    public ReliableNetwork(Random r, Impl<Msg> i, Spec<Msg> s) {
        super(r);
        impl = i;
        spec = s;

        Sorts.IvyInt nodeSort = mkInt("nodeSort", 0, 3);
        Sorts.IvyInt msgSort = mkInt("msgSort", 33, 126);

        send = Action3.from(
                (a, b, c) -> Either.right(null),
                impl::send,
                spec::after_send);
        addAction(send.pipe(() -> new Tuple3(nodeSort.get(), nodeSort.get(), msgSort.get())));

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

        addConjecture("at-most-once-delivery", () -> spec.inFlight >= 0);
        addConjecture("eventual-delivery", () ->
                impl.routingTable.values().stream().allMatch(q -> q.isEmpty()) || spec.inFlight > 0);
    }

    public static abstract class Impl<Msg> extends Network<Integer, Msg> {
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
