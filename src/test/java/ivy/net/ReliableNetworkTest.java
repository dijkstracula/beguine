package ivy.net;

import ivy.exceptions.IvyExceptions;
import org.javatuples.Pair;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class ReliableNetworkTest {

    public void onRecv(Pair<Integer, Integer> args) {
        int id = args.getValue0();
        int val = args.getValue1();
        System.out.println(String.format("Received %d from %d", val, id));
    }
    @Test
    public void testReliableNetwork() throws IvyExceptions.ConjectureFailure {
        Random r = new Random(42);
        ReliableNetwork net = new ReliableNetwork(r, 10, this::onRecv);

        for (int i = 0; i < 100; i++) {
            net.chooseAction().run();
        }
    }
}
