package ivy.MultiCounterTest;

import java.util.ArrayList;

/**
 * Just like NonnegativeCounterTest, but now the counter isolate is parameterised, necessitating a server_id
 * layer of indirection.  This is written how a human might, with a single indirection layer to a per-"node"
 * datatype.  This is in contrast to the reference implementation, which emits an indirection layer for each
 * field in the "node" datatype.
 */
public class MultiCounter {
    ArrayList<Node> nodes;

    public MultiCounter(int max_nodes) {
        nodes = new ArrayList<>();
        for (int i = 0; i < max_nodes; i++) {
            nodes.add(new Node(i));
        }
    }

    void dec(int n) {
        nodes.get(n).dec();
    }
    void inc(int n) {
        nodes.get(n).inc();
    }

    /** An object with a counter that can be incremented or decremented...but must never fall below 0!!! */
    class Node {
        int node;
        int val;

        public Node(int n) {
            node = n;
            val = 0;
        }

        void dec() {
            val -= 1;
            System.out.println(String.format("[node %d] [dec] val = %d", node, val));
        }

        void inc() {
            val += 1;
            System.out.println(String.format("[node %d] [inc] val = %d", node, val));
        }
    }
}

