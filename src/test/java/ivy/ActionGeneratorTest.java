package ivy;

import com.microsoft.z3.*;
import ivy.decls.Decls;
import ivy.sorts.Sorts;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class ActionGeneratorTest {

    /**
     * Morally, a protocol representing a node id on [0, MAX_N).
     */
    private static class TestProtocol {
        public long MAX_N;

        private ArrayList<Boolean> statuses;

        public TestProtocol(int max_n) {
            MAX_N = max_n;
            statuses = new ArrayList<>(Collections.nCopies(max_n, true));
        }

        public void isDown(int n) {
            System.out.println(String.format("Node %d is down", n));
            statuses.set(n, false);
        }
    }

    private class TestActionGen extends ActionGenerator<TestProtocol> {

        public final Sorts.IvyInt nodeSort;
        public final Decls.IvyConst<Long, IntSort, Sorts.IvyInt> node;

        public TestActionGen(Random r, TestProtocol p) {
            super(r, p);
            nodeSort = sorts.mkInt("nodeSort", 0, p.MAX_N);
            node = decls.mkConst("node", nodeSort);
        }

        @Override
        public Model generate(TestProtocol protocol) {
            push();
            add(node.randomize());
            Model m = solve();
            pop();
            return m;
        }

        @Override
        public void execute(TestProtocol protocol, Model m) {
            long n = node.eval(m);
            protocol.isDown((int)n);
        }
    }

    @Test
    public void doit() {
        Random r = new Random(42);
        TestProtocol p = new TestProtocol(42);
        TestActionGen gen = new TestActionGen(r, p);

        // Generating a value for p.n should put it within the right bounds.
        for (int i = 0; i < 1000; i++) {
            Model m = gen.generate(p);
            gen.execute(p, m);
        }
    }
}
