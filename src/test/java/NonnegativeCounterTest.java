import io.vavr.control.Either;
import ivy.Protocol;
import ivy.exceptions.IvyExceptions;
import ivy.functions.Actions.Action0;
import org.junit.jupiter.api.Test;

import java.util.Random;

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

    public class NonnegativeCounterProtocol extends Protocol {
        public NonnegativeCounterProtocol(Random r, NonnegativeCounterImpl impl) {
            super(r);
            addConjecture("non-negativity", () -> impl.val >= 0);
            addAction(Action0.from(impl::dec));
            addAction(Action0.from(impl::inc));
        }
    }

    @Test
    public void testCounter() {
        Random r = new Random(42);
        NonnegativeCounterImpl impl = new NonnegativeCounterImpl();
        NonnegativeCounterProtocol proto = new NonnegativeCounterProtocol(r, impl);

        // At some point, the counter will go negative, invalidating the
        // nonnegativity conjecture.
        while (true) {
            Either<IvyExceptions.ConjectureFailure, Void> res = proto.takeAction();
            if (res.isLeft()) {
                break;
            }
        }
    }
}
