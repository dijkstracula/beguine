package ivy.multicounter;

import com.microsoft.z3.IntSort;
import ivy.Protocol;
import ivy.decls.Decls;
import ivy.exceptions.IvyExceptions;
import ivy.sorts.Sorts;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class MultiCounterTest {
    @Test
    public void testCounter() {
        Random r = new Random(42);
        MultiCounterProto proto = new MultiCounterProto(r, 5);

        assertThrows(IvyExceptions.ConjectureFailure.class, () -> {
            // At some point, one of the nodes' counter will go negative, invalidating the
            // nonnegativity conjecture.
            while (true) {
                proto.chooseAction().run();
            }
        });
    }
}
