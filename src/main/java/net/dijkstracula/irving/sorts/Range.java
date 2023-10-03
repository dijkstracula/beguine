package net.dijkstracula.irving.sorts;

import com.microsoft.z3.Expr;
import com.microsoft.z3.IntSort;
import net.dijkstracula.melina.runtime.MelinaContext;

import java.util.function.Supplier;

public class Range extends IvySort<Long, IntSort> {
    public final long min;
    public final long max;

    public Range(String name, MelinaContext ctx, long min, long max) {
        super(name, ctx);
        assert(min < max);
        this.min = min;
        this.max = max;
    }

    @Override
    public Long make() {
        return min;
    }

    @Override
    public Supplier<Long> generator() {
        return ctx.randomRange(this);
    }

    @Override
    public Expr<IntSort> to_solver(Long val) {
        return ctx.getZ3Ctx().mkInt(val);
    }

    @Override
    public IntSort getZ3Sort() {
        return ctx.getZ3Ctx().getIntSort();
    }
}
