package ivy;

import org.junit.jupiter.api.Test;

import ivy.Conjecture.ConjectureFailure;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class NonnegativeCounterTest {
    /** An isolate with a counter that can be incremented or decremented...but must never fall below 0!!! */
    public class NonnegativeCounterImpl {
        int val;

        void dec() {
            val -= 1;
            System.out.println(String.format("[dec] val = %d", val));
        }
        void inc() {
            val += 1;
            System.out.println(String.format("[inc] val = %d", val));
        }
    }

    public class NonnegativeCounterProtocol extends Protocol<NonnegativeCounterImpl> {
        public NonnegativeCounterProtocol(Random r, NonnegativeCounterImpl impl) {
            super(r);
            addAction(Generator.Unit, impl::dec);
            addAction(Generator.Unit, impl::inc);
            addConjecture("non-negativity", () -> impl.val >= 0);
        }
    }

    @Test
    public void testCounter() {
        Random r = new Random(42);
        NonnegativeCounterImpl impl = new NonnegativeCounterImpl();
        NonnegativeCounterProtocol spec = new NonnegativeCounterProtocol(r, impl);

        assertThrows(ConjectureFailure.class, () -> {
            // At some point, the counter will go negative, invalidating the
            // nonnegativity conjecture.
            while (true) {
                spec.chooseAction().run();
            }
        });
    }
}
