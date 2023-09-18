package net.dijkstracula.irving.stdlib.net;

import com.microsoft.z3.Context;
import net.dijkstracula.irving.sorts.Sorts;
import net.dijkstracula.melina.runtime.Protocol;
import net.dijkstracula.melina.stdlib.net.ReliableNetwork;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PingPongTest{

    public class PingPong extends Protocol {
        private final Sorts sorts;
        private final Sorts.Range pid;

        ReliableNetwork<Long> net;

        public PingPong(Random r) {
            super(r);
            net = new ReliableNetwork<>(r);
            sorts = new Sorts(new Context(), r);
            pid = sorts.mkRange("pid", 0, 2);
            host = IntStream.range(0, 2).mapToObj(i -> new Host(i)).collect(Collectors.toList());
            addAction("recvf", net.recvf, pid);
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
        PingPong pp = new PingPong(new Random(42));
        pp.host.get(0).sock.send.apply(1L, 0L);
        for (int i = 0; i < 1000; i++) {
            pp.run();
        }
    }
}
