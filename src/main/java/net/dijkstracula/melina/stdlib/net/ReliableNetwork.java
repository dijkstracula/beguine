package net.dijkstracula.melina.stdlib.net;

import io.vavr.Tuple3;
import net.dijkstracula.melina.actions.Action1;
import net.dijkstracula.melina.actions.Action2;
import net.dijkstracula.melina.exceptions.ActionArgGenRetryException;
import net.dijkstracula.melina.runtime.Protocol;

import java.util.*;
import java.util.stream.Collectors;

// An implementation of ivy's net.tcp_test.
public class ReliableNetwork<Msg> extends Protocol {

    public final Action1<Long, Socket> dial = new Action1<>();
    public final Action1<Long, Void> recvf = new Action1<>();
    private final HashMap<Long, ArrayDeque<Tuple3<Long, Long, Msg>>> routingTable;

    public int inFlight; // ghost

    public ReliableNetwork(Random r) {
        super(r);

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
        addAction("recvf", recvf, () -> {
            List<Long> keys = sockets.keySet().stream().collect(Collectors.toList());
            return keys.get(r.nextInt(keys.size()));
        });

        // Dial registers a socket with a pid.
        // TODO: "connect" vs "dial"?
        dial.before((id) -> {
            assert(!sockets.containsKey(id));
            return null;
        });
        dial.on((id) -> {
            System.out.println(String.format("[DIAL %d]", id));
            Socket sock = new Socket(r, id);
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

        sockets = new HashMap<>();
    }
    public HashMap<Long, Socket> sockets;


    // A reimplementation of ivy's net.tcp_test.socket, kinda-sorta.
    public class Socket extends Protocol {
        public long id;

        public Action2<Long, Msg, Void> send;

        public Action2<Long, Msg, Void> recv;

        private Socket(Random r, long i) {
            super(r);

            id = i;
            send = new Action2<>();
            recv = new Action2<>(); // Exported

            send.on((dst, msg) -> {
                routingTable.computeIfAbsent(dst, id -> new ArrayDeque<>()).add(new Tuple3<>(dst, i, msg));
                return null;
            });

        }
    }
}
