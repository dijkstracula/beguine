package ivy;

import com.microsoft.z3.*;
import ivy.Conjecture.ConjectureFailure;
import ivy.decls.Decls;
import ivy.functions.ThrowingRunnable;
import ivy.sorts.Sorts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.Random;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public class TrivialFailureDetectorTest {

    /**
     * Morally, an isolate tracking availablity of nodes on [0, MAX_N).
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

    public static class TrivialFailureDetectorSpec extends Specification<TrivialFailureDetector> {
        // Actions
        public final Supplier<Integer> isDownGen;
        public final Consumer<Integer> isDownExec;

        // Ghost state
        public final Decls.IvyConst<Integer, IntSort> node;

        public TrivialFailureDetectorSpec(Random r, TrivialFailureDetector i) {
            super(r, i);
            isDownGen = new IsDownGenerator(this);
            isDownExec = i::isDown;
            addAction(isDownGen, isDownExec);

            Sorts.IvyInt nodeSort = mkInt("nodeSort", 0, i.MAX_N);
            node = mkConst("node", nodeSort);
        }

        /**
         * Generates random events to be passed to TrivialFailureDetector::isDown().
         */
         public class IsDownGenerator extends Generator<TrivialFailureDetector, TrivialFailureDetectorSpec, Integer> {

            public IsDownGenerator(TrivialFailureDetectorSpec spec) {
                super(spec);
            }

            @Override
            protected void randomize() {
                add(specification.node.randomize());
            }

            @Override
            protected Integer eval(Model m) {
                return specification.node.eval(m);
            }
        }
    }

    private TrivialFailureDetector p;
    private TrivialFailureDetectorSpec s;


    @BeforeEach
    public void setup() {
        Random r = new Random(42);
        p = new TrivialFailureDetector(42);
        s = new TrivialFailureDetectorSpec(r, p);
    }

    @Test
    public void testGenAndExec() {
        for (int i = 0; i < 500; i++) {
            // Generating a value for p.n should put it within the right bounds.
            int n = s.isDownGen.get();
            assertTrue(0 <= n);
            assertTrue(n < p.MAX_N);

            // And executing it should have an observable effect on the implementation.
            s.isDownExec.accept(n);
            assertFalse(p.statuses.get(n));
        }
    }

    @Test
    public void testPipe() throws ConjectureFailure {
        // Tests the same thing as testGenAndExec, but with the added layer of composing
        // the source and sink into a thunk.
        ThrowingRunnable<ConjectureFailure> pipe = s.pipe(s.isDownGen, s.isDownExec);

        for (int i = 0; i < 500; i++) {
            pipe.run();
        }

        // Since piping the action values abstracts the particular choices away, we can
        // only observe that after a ~sufficient~ number of iterations, all nodes are
        // downed eventually.
        for (int i = 0; i < p.MAX_N; i++) {
            assertFalse(p.statuses.get(i));
        }
    }

    @Test
    public void testAct() throws ConjectureFailure {
        // Tests the same thing as testPipe, except that the single isDown action is
        // "nondeterministically" chosen by the Specification.
        for (int i = 0; i < 500; i++) {
            s.chooseAction().run();
        }

        // Since piping the action values abstracts the particular choices away, we can
        // only observe that after a ~sufficient~ number of iterations, all nodes are
        // downed eventually.
        for (int i = 0; i < p.MAX_N; i++) {
            assertFalse(p.statuses.get(i));
        }
    }
}
