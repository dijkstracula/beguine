package net.dijkstracula.irving.stdlib.net;

import com.microsoft.z3.Context;
import net.dijkstracula.irving.sorts.Sorts;
import net.dijkstracula.melina.stdlib.net.ReliableNetwork;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReliableNetworkTest {
    private final Random r = new Random(42);
    private final Sorts sorts = new Sorts(new Context(), r);

    @Test
    public void reliableNetworkTestTest() {
        ReliableNetwork<Long> net = new ReliableNetwork<>(r);

        sorts.mkRange("pid", 0, 2).iterator().forEach(i -> {
            ReliableNetwork<Long>.Socket s = net.dial.apply(i);
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
