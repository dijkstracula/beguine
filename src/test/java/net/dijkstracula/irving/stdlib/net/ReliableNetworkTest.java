package net.dijkstracula.irving.stdlib.net;

import net.dijkstracula.melina.runtime.MelinaContext;
import net.dijkstracula.melina.stdlib.net.ReliableNetwork;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

public class ReliableNetworkTest {
    private final MelinaContext ctx = MelinaContext.fromSeed(42);
    @Test
    public void reliableNetworkTestTest() {
        ReliableNetwork<Long> net = new ReliableNetwork<>(ctx);

        IntStream.range(0, 2).forEach(i -> {
            ReliableNetwork<Long>.Socket s = net.dial.apply((long)i);
        });

        AtomicBoolean recvRan = new AtomicBoolean(false);
        net.sockets.get(1L).recv.on((src, msg) -> {
            assert(src == 0);
            assert(msg == 42);
            recvRan.set(true);
            return null;
        });

        net.sockets.get(0L).send.apply(1L, 42L);
        net.run();
        assert(recvRan.get() == true);
    }
}
