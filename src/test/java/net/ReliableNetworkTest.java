package net;

import io.vavr.Tuple3;
import ivy.Protocol;
import ivy.net.ReliableNetwork;
import ivy.sorts.Sorts;

import java.util.Random;

public class ReliableNetworkTest extends Protocol {

    ReliableNetwork.Impl<Integer, Byte> netImpl = new ReliableNetwork.Impl<Integer, Byte>() {
        @Override
        public Void recv(Integer self, Integer src, Byte msg) {
            System.out.println(String.format("[Net %3d]: Received '%c' from %d", self, src, src));
            return null;
        }
    };
    ReliableNetwork.Spec<Integer, Byte> netSpec = new ReliableNetwork.Spec();

    public ReliableNetworkTest(Random r) {
        super(r);


        ReliableNetwork<Integer, Byte> net = new ReliableNetwork<>(r, netImpl, netSpec);

        Sorts.IvyInt nodeSort = mkInt("nodeSort", 0, 3);
        Sorts.IvyInt msgSort = mkInt("msgSort");

        addAction(() -> {
                    int id = nodeSort.get();
                    int dst = nodeSort.get();
                    int msg = msgSort.get();
                    return new Tuple3(id, dst, msg);
                },
                net.send);

        /*
        addAction(
                nodeSort::get,
                dst -> {
                    Pair<Id, Msg> p = impl.routingTable.get(dst).pop();
                    Id src = p.getValue0();
                    Msg msg = p.getValue1();
                    impl.recv(dst, src, msg);
                }
        );
        ReliableNetwork net = new ReliableNetwork(r, 10, this::onRecv);

        for (int i = 0; i < 100; i++) {
            net.chooseAction().run();
        }
                 */
    }
}
