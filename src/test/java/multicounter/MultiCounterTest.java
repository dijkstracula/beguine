package multicounter;

import io.vavr.control.Either;
import ivy.exceptions.IvyExceptions.*;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class MultiCounterTest {
    @Test
    public void testCounter() {
        Random r = new Random(42);
        MultiCounterProto proto = new MultiCounterProto(r, 5);

        // At some point, one of the nodes' counter will go negative, invalidating the
        // nonnegativity conjecture.
        while (true) {
            Either<ConjectureFailure, Void> res = proto.takeAction();
            if (res.isLeft()) {
                ConjectureFailure e = res.getLeft();
                assert(e.getMessage().contains("non-negativity"));
                assert(e.conj.getDesc().equals("non-negativity"));
                break;
            }
        }
    }
}
