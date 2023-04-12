package ivy.sorts;

import com.microsoft.z3.*;

import java.util.List;
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

    public IvyChar mkPrintableAscii(String name) { return new IvyChar(
            name,
            () -> (char) (33 + random.nextInt(127-33)),
            e -> ctx.mkGe(e, ctx.mkInt(33)),
            e -> ctx.mkLt(e, ctx.mkInt(128))
            );
    }

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

        IvyInt(String name, Supplier<Integer> f, Constraint<IntSort>... constraints) {
            super(name, f, constraints);
        }

        @Override
        public Expr<IntSort> to_solver(Integer val) {
            return ctx.mkInt(val);
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

    public class IvyChar extends IvySort<Character, IntSort> {

        IvyChar(String name, Supplier<Character> f, Constraint<IntSort>... constraints) {
            super(name, f, constraints);
        }

        @Override
        public Expr<IntSort> to_solver(Character val) {
            return ctx.mkInt(val);
        }

        @Override
        public IntSort getZ3Sort() {
            return ctx.getIntSort();
        }

        @Override
        public Character eval(Model m, Expr<IntSort> expr) {
            IntNum evaled = (IntNum) m.eval(expr, false);
            return (char)(evaled.getInt());
        }
    }

    /*
    // My kingdom for one or more of:
    // 1. Typeclasses; 2. Type argument inference; 3. a macro system
    public class Tuple2<
            J1, Z1 extends Sort,
            J2, Z2 extends Sort,
            T extends IvySort<J1, Z1> ,U extends IvySort<J2, Z2>>
            extends IvySort<org.javatuples.Pair<T, U>, TupleSort> {

        TupleSort sort;

        public Tuple2(String name, Supplier<org.javatuples.Pair<T, U>> f) {
            super(name, f);

            org.javatuples.Pair<T, U> val = f.get();
            T t = val.getValue0();
            U u = val.getValue1();

            // TODO: priming the pump like this seems a bit suspect.
            sort = ctx.mkTupleSort(
                    ctx.mkSymbol(name),
                    new Symbol[]{ctx.mkSymbol("val1"), ctx.mkSymbol("val2")},
                    new Sort[]{t.getZ3Sort(), u.getZ3Sort()});
        }

        @Override
        public Expr<TupleSort> to_solver(org.javatuples.Pair<T, U> val) {
            T t = val.getValue0();
            U u = val.getValue1();

            String name = String.format("Tuple%d[%s, %s]", t.getZ3Sort().getName(), u.getZ3Sort().getName());

            if (sort == null) {
                sort = ctx.mkTupleSort(
                        ctx.mkSymbol(name),
                        new Symbol[]{ctx.mkSymbol("val1"), ctx.mkSymbol("val2")},
                        new Sort[]{t.getZ3Sort(), u.getZ3Sort()});
            }
            FuncDecl<TupleSort> f = sort.mkDecl();
            return f.apply(
                    t.to_solver(t.get()),
                    u.to_solver(u.get()));
        }

        @Override
        public TupleSort getZ3Sort() {
            return null;
        }

        @Override
        public org.javatuples.Pair<T, U> eval(Model m, Expr<TupleSort> e) {
            return null;
        }
    }
     */

}
