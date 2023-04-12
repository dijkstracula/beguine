package multicounter;

import com.microsoft.z3.IntSort;
import ivy.decls.Decls;
import ivy.Protocol;
import ivy.functions.Actions.Action1;
import ivy.sorts.Sorts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MultiCounterProto extends Protocol {
    private RefImpl impl;
    private Spec spec;

    private int max_nodes;

    public MultiCounterProto(Random r, int n) {
        super(r);
        max_nodes = n;
        impl = new RefImpl();
        spec = new Spec();
    }

    /**
     * Just like NonnegativeCounterTest, but now the counter isolate is parameterised, necessitating a server_id
     * layer of indirection.  This is written how a human might, with a single indirection layer to a per-"node"
     * datatype.  This is in contrast to the reference implementation, which emits an indirection layer for each
     * field in the "node" datatype.
     */
    public class Impl {
        ArrayList<Node> nodes;

        public Impl() {
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

        /**
         * An object with a counter that can be incremented or decremented...but must never fall below 0!!!
         */
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


    /**
     * This is closer to how Ivy generates parameterized objects.  Each field is accessed indirectly
     * through a per-field associative array, keyed on the parameter type.
     */
    public class RefImpl {
        HashMap<Integer, Integer> node;
        HashMap<Integer, Integer> val;

        public final int max_n;

        public RefImpl() {
            max_n = max_nodes;
            node = new HashMap<>();
            val = new HashMap<>();

            for (int i = 0; i < max_nodes; i++) {
                node.put(i, i);
                val.put(i, 3);
            }
        }

        void dec(int n) {
            val.put(n, val.get(n) - 1);
            System.out.println(String.format("[RefImpl %3d] [dec] val = %d", node.get(n), val.get(n)));
        }

        void inc(int n) {
            val.put(n, val.get(n) + 1);
            System.out.println(String.format("[RefImpl %3d] [inc] val = %d", node.get(n), val.get(n)));
        }
    }

    public class Spec {
        final Decls.IvyConst<Integer, IntSort> node;
        final Decls.IvyConst<Integer, IntSort> val;

        public Spec() {
            Sorts.IvyInt nodeSort = sorts.mkInt("nodeSort", 0, impl.max_n);
            node = decls.mkConst("node", nodeSort);
            val = decls.mkConst("val", sorts.mkInt("Int"));

            addAction(Action1.from(impl::dec).pipe(nodeSort));
            addAction(Action1.from(impl::inc).pipe(nodeSort));

            // NB: In Ivy-generated C++:
            // 1291         for (int V0 = 0; V0 < server_id__max+1; V0++) {
            // 1292             if (!((0 < mutator__count[V0]) || (mutator__count[V0] == 0))) __tmp2 = 0;
            // 1293         }
            addConjecture("non-negativity", () -> impl.val.values().stream().allMatch(i -> i >= 0));
        }
    }
}

