package net;

import io.vavr.control.Either;
import ivy.Protocol;
import ivy.exceptions.IvyExceptions;
import ivy.net.ReliableNetwork;

import org.junit.jupiter.api.Test;

import java.util.Random;

public class ReliableNetworkTest {

    public class NetTest extends Protocol {

        public ReliableNetwork<Integer> net;

        public int recvCount;

        public class NetImpl extends ReliableNetwork.Impl<Integer> {
            @Override
            public Void recv(Integer self, Integer src, Integer msg) {
                System.out.println(String.format("[Net %3d] RECV '%c' from %d", self, msg, src));
                recvCount++;
                return null;
            }
        }

        public class NetSpec extends ReliableNetwork.Spec<Integer> {}

        public NetTest(Random r) {
            super(r);
            net = new ReliableNetwork<>(r, new NetImpl(), new NetSpec());
            recvCount = 0;
            combine(net);
        }
    }

    @Test
    public void testProtocolInheritance() {
        NetTest n = new NetTest(new Random(42));
        assert(n.getConjectures().stream().anyMatch(c -> c.getDesc().equals("at-most-once-delivery")));
        assert(n.getConjectures().stream().anyMatch(c -> c.getDesc().equals("eventual-delivery")));
    }

    @Test
    public void testActionGen() {
        NetTest n = new NetTest(new Random(42));

        for (int i = 0; i < 100; i++) {
            Either<IvyExceptions.ConjectureFailure, Void> res = n.takeAction();
            assert(res.isRight());
        }
        assert(n.recvCount > 0);
    }
}
