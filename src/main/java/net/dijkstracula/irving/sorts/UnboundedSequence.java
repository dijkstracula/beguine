package net.dijkstracula.irving.sorts;

import com.microsoft.z3.Expr;
import com.microsoft.z3.IntSort;
import net.dijkstracula.melina.runtime.MelinaContext;

import java.util.function.Supplier;

public class UnboundedSequence extends IvySort<Long, IntSort> {
    public UnboundedSequence(MelinaContext ctx) {
        super("unbounded_sequence", ctx);
    }

    @Override
    public Long make() {
        return 0L;
    }

    @Override
    public Supplier<Long> generator() {
        return ctx.randomSmallNat();
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
