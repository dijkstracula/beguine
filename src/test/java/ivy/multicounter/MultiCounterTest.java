package ivy.multicounter;

import ivy.exceptions.IvyExceptions;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class MultiCounterTest {

    @Test
    public void testCounter() {
        Random r = new Random(42);
        MultiCounterRefImpl impl = new MultiCounterRefImpl(10);
        MultiCounterProto spec = new MultiCounterProto(r, impl);

        assertThrows(IvyExceptions.ConjectureFailure.class, () -> {
            // At some point, one of the nodes' counter will go negative, invalidating the
            // nonnegativity conjecture.
            while (true) {
                spec.chooseAction().run();
            }
        });
    }
}
