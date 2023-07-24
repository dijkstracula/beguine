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

        Either<IvyExceptions.ActionException, Void> dec() {
            val -= 1;
            System.out.println(String.format("[dec] val = %d", val));
            return Either.right(null);
        }
        Either<IvyExceptions.ActionException, Void> inc() {
            val += 1;
            System.out.println(String.format("[inc] val = %d", val));
            return Either.right(null);
        }
    }

    public class NonnegativeCounterProtocol extends Protocol {
        public NonnegativeCounterProtocol(Random r, NonnegativeCounterImpl impl) {
            super(r);
            addConjecture("non-negativity", () -> impl.val >= 0);
            addAction(new Action0("dec", impl::dec));
            addAction(new Action0("inc", impl::inc));
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
