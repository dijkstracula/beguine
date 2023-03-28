package ivy.multicounter;

import java.util.HashMap;

/** This is closer to how Ivy generates parameterized objects.  Each field is accessed indirectly
 *  through a per-field associative array, keyed on the parameter type. */
public class MultiCounterRefImpl {

    HashMap<Integer, Integer> node;
    HashMap<Integer, Integer> val;

    public final int max_n;

    public MultiCounterRefImpl(int max_nodes) {
        max_n = max_nodes;
        node = new HashMap<>();
        val = new HashMap<>();

        for (int i = 0; i < max_nodes; i++) {
            node.put(i, i);
            val.put(i, 1);
        }
    };

    void dec(int n) {
        val.put(n, val.get(n) - 1);
        System.out.println(String.format("[node %d] [dec] val = %d", node.get(n), val.get(n)));
    }
    void inc(int n) {
        val.put(n, val.get(n) + 1);
        System.out.println(String.format("[node %d] [dec] val = %d", node.get(n), val.get(n)));
    }
}
