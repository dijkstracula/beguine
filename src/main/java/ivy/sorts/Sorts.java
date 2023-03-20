package ivy.sorts;

import com.microsoft.z3.*;

import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.function.Supplier;

public class Sorts {
    private final Context ctx;
    private final Random random;

    public Sorts(Context ctx, Random random) {
        this.ctx = ctx;
        this.random = random;
    }

    public IvyBool mkBool(String name) { return mkBool(name, random::nextBoolean); }
    public IvyBool mkBool(String name, Supplier<Boolean> f) { return new IvyBool(name, f); }

    public IvyInt mkInt(String name) { return mkInt(name, random::nextInt); }
    public IvyInt mkInt(String name, Supplier<Integer> f) { return new IvyInt(name, f); }
    public IvyInt mkInt(String name, int min, int max) {
        if (min >= max) {
            throw new RuntimeException(String.format("Invalid range [%d, %d)", min, max));
        }
        PrimitiveIterator.OfInt rands = random.ints(min, max).iterator();
        return new IvyInt(
                name,
                () -> rands.next(),
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

    public class IvyInt extends IvySort<Integer, IntSort> {

        IvyInt(String name, Supplier<Integer> f, Constraint<Integer, IntSort>... constraints) {
            super(name, f, constraints);
        }

        @Override
        public Expr<IntSort> to_solver(Integer val) {
            return ctx.mkInt(val.toString());
        }

        @Override
        public IntSort getZ3Sort() {
            return ctx.getIntSort();
        }

        @Override
        public Integer eval(Model m, Expr<IntSort> expr) {
            IntNum evaled = (IntNum) m.eval(expr, false);
            return evaled.getInt();
        }
    }
}
