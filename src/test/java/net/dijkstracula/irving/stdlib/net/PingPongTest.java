package net.dijkstracula.irving.stdlib.net;

import net.dijkstracula.irving.sorts.Range;
import net.dijkstracula.melina.runtime.MelinaContext;
import net.dijkstracula.melina.runtime.Protocol;
import net.dijkstracula.melina.stdlib.net.ReliableNetwork;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PingPongTest{

    public class PingPong extends Protocol {
        private final Range pid;

        ReliableNetwork<Long> net;

        public PingPong(MelinaContext ctx) {
            super(ctx);
            pid = ctx.mkRange("pid", 0, 2);

            net = new ReliableNetwork<>(ctx);
            exportAction("net.recvf", net.recvf, ctx.randomSelect(net.sockets.keySet().stream().toList()));

            host = IntStream.range(0, 2).mapToObj(i -> new Host(i)).collect(Collectors.toList());
        }

        public class Host {
            ReliableNetwork<Long>.Socket sock;

            long self;
            public Host(long self) {
                this.self = self;
                sock = net.dial.apply(self);
                sock.recv.on((src, msg) -> {
                    System.out.println(String.format("[Node %d]: recv %d from %d", self, msg, src));
                    sock.send.apply(src, msg+1);
                    return null;
                });
            }
        }
        List<Host> host;

    }

    @Test
    public void testPingPong() {
        PingPong pp = new PingPong(MelinaContext.fromSeed(42));
        pp.host.get(0).sock.send.apply(1L, 0L);
        for (int i = 0; i < 10; i++) {
            pp.run();
        }
    }
}
