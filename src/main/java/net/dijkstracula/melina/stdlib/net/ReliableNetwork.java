package net.dijkstracula.melina.stdlib.net;

import io.vavr.Tuple3;
import net.dijkstracula.melina.actions.Action1;
import net.dijkstracula.melina.actions.Action2;
import net.dijkstracula.melina.exceptions.ActionArgGenRetryException;
import net.dijkstracula.melina.history.ActionCall;
import net.dijkstracula.melina.runtime.MelinaContext;
import net.dijkstracula.melina.runtime.Protocol;

import java.util.*;

// An implementation of ivy's net.tcp_test.
public class ReliableNetwork<Msg> extends Protocol {

    public final Action1<Long, Socket> dial = new Action1<>();
    public final Action1<Long, Void> recvf = new Action1<>();
    public final HashMap<Long, Socket> sockets;
    private final HashMap<Long, ArrayDeque<Tuple3<Long, Long, Msg>>> routingTable;

    public int inFlight; // ghost

    public ReliableNetwork(MelinaContext ctx) {
        super(ctx);


        sockets = new HashMap<>();
        routingTable = new HashMap<>();

        // recvf is triggered when the underlying network is ready to deliver a message.
        recvf.before((id) -> {
            if (!routingTable.containsKey(id)) {
                throw new ActionArgGenRetryException();
            }
            if (routingTable.get(id).size() == 0) {
                throw new ActionArgGenRetryException();
            }
            return null;
        });
        recvf.on((id) -> {
            Tuple3<Long, Long, Msg> wrappedMsg = routingTable.get(id).pop();
            long source = wrappedMsg._2;
            Msg msg = wrappedMsg._3;
            return sockets.get(id).recv.apply(source, msg);
        });

        // Dial registers a socket with a pid.
        // TODO: "connect" vs "dial"?
        dial.before((id) -> {
            assert(!sockets.containsKey(id));
            return null;
        });
        dial.on((id) -> {
            System.out.println(String.format("[DIAL %d]", id));
            Socket sock = new Socket(ctx, id);
            sockets.put(id, sock);
            return sock;
        });
        dial.after((id, socket) -> {
            assert(sockets.containsKey(id));
            return null;
        });

        addConjecture("reliable-network-at-most-once-delivery", () -> inFlight >= 0);
        addConjecture("reliable-network-eventual-delivery", () ->
                routingTable.values().stream().allMatch(q -> q.isEmpty()) || inFlight > 0);

        exportAction("recvf", recvf, ctx.randomSelect(sockets.keySet().stream().toList()));
    }

    // A reimplementation of ivy's net.tcp_test.socket, kinda-sorta.
    public class Socket extends Protocol {
        public long id;

        public Action2<Long, Msg, Void> send;

        public Action2<Long, Msg, Void> recv;

        private Socket(MelinaContext ctx, long i) {
            super(ctx);

            id = i;
            send = new Action2<>();
            recv = new Action2<>(); // Exported

            send.on((dst, msg) -> {
                routingTable.computeIfAbsent(dst, id -> new ArrayDeque<>()).add(new Tuple3<>(dst, i, msg));
                return null;
            });
        }

        @Override
        public String toString() {
            return "Socket{" +
                    "id=" + id +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Socket socket = (Socket) o;
            return id == socket.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}
