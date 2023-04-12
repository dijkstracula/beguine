package net;

import io.vavr.Tuple3;
import io.vavr.control.Either;
import ivy.Protocol;
import ivy.exceptions.IvyExceptions;
import ivy.functions.Actions.Action3;
import ivy.net.ReliableNetwork;

import ivy.sorts.Sorts;
import org.junit.jupiter.api.Test;

import java.io.CharConversionException;
import java.util.Random;

public class ReliableNetworkTest {

    public class NetTest extends Protocol {
        public final Action3<Integer, Integer, Character, Void> send;
        public int recvCount;

        public NetTest(Random r) {
            super(r);

            Sorts.IvyInt nodeSort = sorts.mkInt("nodeSort", 0, 3);
            Sorts.IvyChar msgSort = sorts.mkPrintableAscii("msgSort");

            ReliableNetwork.Impl<Character> net_impl = new NetImpl();
            ReliableNetwork.Spec net_spec = new NetSpec();
            ReliableNetwork<Character> net = new ReliableNetwork<>(r, net_impl, net_spec);

            recvCount = 0;
            combine(net);

            send = Action3.from(
                    (a, b, c) -> Either.right(null),
                    net_impl::send,
                    net_spec::after_send);
            addAction(send.pipe(() -> new Tuple3(nodeSort.get(), nodeSort.get(), msgSort.get())));
        }

        public class NetImpl extends ReliableNetwork.Impl<Character> {

            @Override
            public Void recv(Integer self, Integer src, Character msg) {
                System.out.println(String.format("[Net %3d] RECV '%c' from %d", self, msg, src));
                recvCount++;
                return null;
            }
        }

        public class NetSpec extends ReliableNetwork.Spec<Character> {}

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
