package net.dijkstracula.melina.runtime;

import com.microsoft.z3.Context;
import net.dijkstracula.irving.sorts.Range;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Top-level state needed to drive protocols.
 */
public class MelinaContext {
    private final Random random;

    private final Context z3Ctx = new Context();

    private MelinaContext(Random r) {
        this.random = r;
    }

    public static MelinaContext fromSeed(int seed) {
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

    public Supplier<Long> randomRange(Range r) {
        return () -> random.nextLong(r.min, r.max);
    }

    public Supplier<Long> randomSmallNat() {
        return () -> random.nextLong(10);
    }

    public <T> Supplier<T> randomSelect(List<T> seq) {
        return () -> seq.get(random.nextInt(0, seq.size()));
    }

    public <K, V> Supplier<V> randomSelect(Map<K, V> map) {
        return () -> {
            List<V> values = map.values().stream().collect(Collectors.toList());
            return values.get(random.nextInt(0, values.size()));
        };
    }
}
