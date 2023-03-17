package ivy.sorts;

import com.microsoft.z3.*;
import ivy.decls.Decls;

import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.LongStream;

public class Sorts {
    private final Context ctx;
    private final Random random;

    public Sorts(Context ctx, Random random) {
        this.ctx = ctx;
        this.random = random;
    }

    public IvyBool mkBool(String name) { return mkBool(name, () -> random.nextBoolean()); }
    public IvyBool mkBool(String name, Supplier<Boolean> f) { return new IvyBool(name, f); }

    public IvyInt mkInt(String name) { return mkInt(name, () -> random.nextLong()); }
    public IvyInt mkInt(String name, Supplier<Long> f) { return new IvyInt(name, f); }
    public IvyInt mkInt(String name, long min, long max) {
        if (min >= max) {
            throw new RuntimeException(String.format("Invalid range [%d, %d)", min, max));
        }
        LongStream rands = random.longs(min, max);
        return new IvyInt(
                name,
                () -> rands.findFirst().getAsLong(),
                e -> ctx.mkGe(e, ctx.mkInt(min)),
                e -> ctx.mkLt(e, ctx.mkInt(max)));
    }

    public class IvyBool extends IvySort<Boolean, BoolSort> {

        IvyBool(String name, Supplier<Boolean> f) {
            super(name, f);
        }

        @Override
        public Expr<BoolSort> to_solver(Boolean val) {
            return ctx.mkBool(val);
        }

        @Override
        public BoolSort getZ3Sort() {
            return ctx.getBoolSort();
        }

        @Override
        public Boolean eval(Model m, Expr<BoolSort> expr) {
            Expr<BoolSort> evaled = m.eval(expr, false);
            switch (evaled.getBoolValue()) {
                case Z3_L_TRUE:
                    return true;
                case Z3_L_FALSE:
                    return false;
                default:
                    throw new RuntimeException("Uh oh, undef!  TODO: what should we do here");
            }
        }
    }

    public class IvyInt extends IvySort<Long, IntSort> {

        IvyInt(String name, Supplier<Long> f, Constraint<Long, IntSort>... constraints) {
            super(name, f, constraints);
        }

        @Override
        public Expr<IntSort> to_solver(Long val) {
            return ctx.mkInt(val);
        }

        @Override
        public IntSort getZ3Sort() {
            return ctx.getIntSort();
        }

        @Override
        public Long eval(Model m, Expr<IntSort> expr) {
            IntNum evaled = (IntNum) m.eval(expr, false);
            return evaled.getInt64();
        }
    }
}
