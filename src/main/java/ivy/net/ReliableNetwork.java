package ivy.net;

import io.vavr.Tuple2;
import ivy.exceptions.IvyExceptions;
import ivy.functions.Actions.Action3;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Optional;
import java.util.Random;

public class ReliableNetwork<Id extends Comparable<Id>, Msg> extends ivy.Protocol {
    public final Impl<Id, Msg> impl;
    public final Spec<Id, Msg> spec;

    public final Action3<Id, Id, Msg, Void> send;
    public final Action3<Id, Id, Msg, Void> recv;


    public ReliableNetwork(Random r, Impl<Id, Msg> i, Spec<Id, Msg> s) {
        super(r);
        impl = i;
        spec = s;

        send = new Action3<>(
                Optional.empty(),
                impl::send,
                Optional.of(spec::after_send));
        recv = new Action3<>(
                Optional.empty(),
                impl::recv,
                Optional.of(spec::after_recv));

        addConjecture("at-most-once-delivery", () -> spec.inFlight >= 0);
        addConjecture("eventual-delivery", () ->
                impl.routingTable.values().stream().allMatch(q -> q.isEmpty()) || spec.inFlight > 0);
    }

    public static abstract class Impl<Id extends Comparable<Id>, Msg> extends Network<Id, Msg> {
        private final HashMap<Id, ArrayDeque<Tuple2<Id, Msg>>> routingTable;

        public Impl() {
            routingTable = new HashMap<>();
        }

        @Override
        public Void send(Id self, Id dst, Msg msg) {
            System.out.println(String.format("[Net %s] SEND %s", self, dst));
            routingTable.computeIfAbsent(dst, id -> new ArrayDeque<>()).add(new Tuple2<>(self, msg));
            return null;
        }

        public abstract Void recv(Integer self, Integer src, Byte msg);
    }

    public static class Spec<Id extends Comparable<Id>, Msg> {
        public int inFlight;

        public Spec() {
            inFlight = 0;
        }

        public Optional<IvyExceptions.ActionException> after_send(Id self, Id dst, Msg msg, Void ret) {
            inFlight++;
            return Optional.empty();
        }

        public Optional<IvyExceptions.ActionException> after_recv(Id self, Id dst, Msg msg, Void ret) {
            inFlight--;
            return Optional.empty();
        }
    }
}
