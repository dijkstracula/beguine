package net.dijkstracula.irving.stdlib.net;

import com.microsoft.z3.Context;
import net.dijkstracula.irving.sorts.Sorts;
import net.dijkstracula.melina.runtime.ProtocolDriver;
import net.dijkstracula.melina.stdlib.net.ReliableNetwork;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class ReliableNetworkTest {
    private final Random r = new Random(42);
    private final Sorts sorts = new Sorts(new Context(), r);

    @Test
    public void reliableNetworkTestTest() {
        ReliableNetwork<Long> net = new ReliableNetwork<>(sorts.mkRange("pid", 0, 2));

        ProtocolDriver d = new ProtocolDriver(new Random(0), net);

        net.sockets.get(1).recv.on((src, msg) -> {
            assert(src == 0);
            assert(msg == 42);
            return null;
        });

        net.sockets.get(0).send.apply(1L, 42L);
        d.run();
    }
}
