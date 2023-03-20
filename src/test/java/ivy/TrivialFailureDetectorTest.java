package ivy;

import com.microsoft.z3.*;
import ivy.decls.Decls;
import ivy.sorts.Sorts;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class TrivialFailureDetectorTest {

    /**
     * Morally, a protocol tracking availablity of nodes on [0, MAX_N).
     */
    private static class TrivialFailureDetector {
        public int MAX_N;

        private ArrayList<Boolean> statuses;

        public TrivialFailureDetector(int max_n) {
            MAX_N = max_n;
            statuses = new ArrayList<>(Collections.nCopies(max_n, true));
        }

        public void isDown(int n) {
            System.out.println(String.format("Node %d is down", n));
            statuses.set(n, false);
        }
    }

    /**
     * Generates random events to be passed to TrivialFailureDetector::isDown().
     */
    private class IsDownGenerator extends Generator<TrivialFailureDetector, Integer> {
        public final Decls.IvyConst<Integer, IntSort, Sorts.IvyInt> node;

        public IsDownGenerator(Random r, TrivialFailureDetector p) {
            super(r, p);
            Sorts.IvyInt nodeSort = mkInt("nodeSort", 0, p.MAX_N);
            node = mkConst("node", nodeSort);
        }

        @Override
        protected void randomize() {
            add(node.randomize());
        }

        @Override
        protected Integer eval(Model m) {
            return node.eval(m);
        }
    }

    @Test
    public void doit() {
        Random r = new Random(42);
        TrivialFailureDetector p = new TrivialFailureDetector(42);

        IsDownGenerator gen = new IsDownGenerator(r, p);
        Consumer<Integer> executor = p::isDown;

        for (int i = 0; i < 1000; i++) {
            // Generating a value for p.n should put it within the right bounds.
            int n = gen.get();
            assertTrue(0 <= n);
            assertTrue(n < p.MAX_N);

            // And executing it should have an observable effect on the implementation.
            executor.accept(n);
            assertFalse(p.statuses.get(n));
        }
    }
}
