import ivy.net.ReliableNetwork;

import java.util.HashMap;
import java.util.List;

public class ChainRep {
    ReliableNetwork.Impl net;
    int max_nodes;

    class Impl {
        HashMap<Integer, List<Integer>> file;

        void append(int self, int value) {
            file.get(self).add(value);
        }
    }
}
