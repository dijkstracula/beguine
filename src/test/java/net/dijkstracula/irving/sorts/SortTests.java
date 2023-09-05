package net.dijkstracula.irving.sorts;

import com.microsoft.z3.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.Random;

public class SortTests {

    Context ctx = new Context();
    Random random = new Random(42);
    Sorts manager = new Sorts(ctx, random);

    @Test
    public void testUnboundedSequence() {
        Sorts.UnboundedSequence nat = manager.mkUnboundedSequnce();

        for (int i = 0; i < 1000; i++) {
            long l = nat.get();
            Assertions.assertTrue(l >= 0);
        }
    }

    @Test
    public void testRange() {
        Sorts.Range pid = manager.mkRange("pid", 0, 3);

        for (int i = 0; i < 1000; i++) {
            long l = pid.get();
            Assertions.assertTrue(l >= 0 && l < 3);
        }
    }
}
