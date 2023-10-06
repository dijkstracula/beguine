package net.dijkstracula.melina.runtime;

import com.microsoft.z3.Context;
import net.dijkstracula.irving.sorts.Range;
import net.dijkstracula.melina.utils.Random;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Top-level state needed to drive protocols.
 */
public class MelinaContext {
    private final Random random;

    private final Context z3Ctx = new Context();

    private MelinaContext(Random r) {
        this.random = r;
    }

    public static MelinaContext fromSeed(long seed) {
        return new MelinaContext(new Random(seed));
    }

    //

    public Range mkRange(String name, long min, long max) {
        return new Range(name, this, min, max);
    }

    //

    public Context getZ3Ctx() {
        return z3Ctx;
    }

    public Supplier<Long> randomBounded(long min, long max) {
        return () -> random.nextBounded(min, max);
    }

    public Supplier<Long> randomRange(Range r) {
        return () -> random.nextBounded(r.min, r.max);
    }

    public Supplier<Long> randomSmallNat() {
        return () -> random.nextBounded(0, 10);
    }

    public <T> Supplier<T> randomSelect(List<T> seq) {
        return () -> random.nextElm(seq);
    }

    public <K, V> Supplier<V> randomSelect(Map<K, V> map) {
        return () -> random.nextElm(map).getValue();
    }
}
