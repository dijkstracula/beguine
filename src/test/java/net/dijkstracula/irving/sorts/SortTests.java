package net.dijkstracula.irving.sorts;

import net.dijkstracula.melina.runtime.MelinaContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.function.Supplier;

public class SortTests {

    MelinaContext ctx = MelinaContext.fromSeed(42);

    @Test
    public void testUnboundedSequence() {
        Supplier<Long> seqProducer = ctx.randomSmallNat();
        for (int i = 0; i < 1000; i++) {
            long l = seqProducer.get();
            Assertions.assertTrue(l >= 0);
        }
    }

    @Test
    public void testRange() {
        Range r = ctx.mkRange("pid", 0, 3);
        Supplier<Long> rangeSupplier = ctx.randomRange(r);
        for (int i = 0; i < 1000; i++) {
            long l = rangeSupplier.get();
            Assertions.assertTrue(l >= 0 && l < 3);
        }
    }
}
