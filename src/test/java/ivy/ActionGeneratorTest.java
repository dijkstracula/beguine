package ivy;

import com.microsoft.z3.*;
import ivy.ActionGenerator;
import ivy.decls.Decls;
import ivy.sorts.Sorts;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ActionGeneratorTest {

    /**
     * Morally, a protocol representing a node id on [0, MAX_N).
     */
    private static class TestProtocol {
        public long n;
        public long MAX_N = 42;

    }

    private class TestActionGen extends ActionGenerator<TestProtocol> {

        public final Sorts.IvyInt nodeSort;
        public final Decls.IvyConst<Long, IntSort, Sorts.IvyInt> node;

        public TestActionGen(TestProtocol p) {
            nodeSort = sorts.mkInt("nodeSort", 0, p.MAX_N);
            node = decls.mkConst("node", nodeSort);
        }

        @Override
        public boolean generate(TestProtocol protocol) {
            push();
            add(node.randomize());
            Model m = solve();
            pop();

            protocol.n = node.eval(m);
            return true; //TODO: ???
        }

        @Override
        public void execute(TestProtocol protocol) {
        }
    }

    @Test
    public void doit() {
        TestProtocol p = new TestProtocol();
        TestActionGen gen = new TestActionGen(p);

        p.n = -1;
        // Generating a value for p.n should put it within the right bounds.
        gen.generate(p);
        assertTrue(p.n >= 0 && p.n < p.MAX_N);
    }
}
