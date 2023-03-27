package ivy.MultiCounterTest;

import ivy.Conjecture;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class MultiCounterTest {

    @Test
    public void testCounter() {
        Random r = new Random(42);
        MultiCounterRefImpl impl = new MultiCounterRefImpl(10);
        MultiCounterSpec spec = new MultiCounterSpec(r, impl);

        assertThrows(Conjecture.ConjectureFailure.class, () -> {
            // At some point, the counter will go negative, invalidating the
            // nonnegativity conjecture.
            while (true) {
                spec.chooseAction().run();
            }
        });
    }
}
