package net.dijkstracula.irving.stdlib.net;

import com.microsoft.z3.Context;
import net.dijkstracula.irving.sorts.Sorts;
import net.dijkstracula.melina.actions.Action2;
import net.dijkstracula.melina.runtime.Protocol;
import net.dijkstracula.melina.runtime.ProtocolDriver;
import net.dijkstracula.melina.runtime.Tee;
import net.dijkstracula.melina.stdlib.net.ReliableNetwork;
import net.dijkstracula.runtime.nway.FourBitCounter;
import net.dijkstracula.runtime.protocols.NonNegativeCounter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class PingPong extends Protocol {
    private static final Random r = new Random(42);
    private static final Sorts sorts = new Sorts(new Context(), r);
    private static final Sorts.Range pid = sorts.mkRange("pid", 0, 2);

    ReliableNetwork<Long> net = new ReliableNetwork();

    public PingPong() {
        addAction("recvf", net.recvf, pid);
    }

    public class Host {
        ReliableNetwork.Socket sock;

        long self;
        public Host(long self) {
            this.self = self;
            sock = net.dial.apply(self);
            sock.recv.on((src, msg) -> {
                System.out.println(String.format("[Node %d]: recv %d from %d", self, msg, src));
                sock.send.apply(src, msg);
                return null;
            });
        }
    }
    List<Host> host = pid.iterator().mapToObj(i -> new Host(i)).collect(Collectors.toList());

    @Test
    public void testPingPong() {
        ProtocolDriver d = new ProtocolDriver(new Random(0), this);

        host.get(0).sock.send.apply(1L, 42L);
        for (int i = 0; i < 1000; i++) {
            d.run();
        }
    }
}
