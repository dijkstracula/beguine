package ivy.decls;

import com.microsoft.z3.*;
import ivy.sorts.IvySort;
import ivy.sorts.Sorts;

import java.util.Random;

/**
 * Factory for constructing consts and decls.
 */
public class Decls {
    private final Context ctx;
    private final Sorts sorts;
    private final Random random;

    public Decls(Context ctx, Sorts sorts, Random random) {
        this.ctx = ctx;
        this.sorts = sorts;
        this.random = random;
    }

    public <J, Z extends Sort> IvyConst<J, Z> mkConst(String name, IvySort<J, Z> sort) {
        return new IvyConst<>(name, sort);
    }

    /**
     * A zero-arity decl (aka a const).
     * @param <J> The Java type that this const value is represented by.
     * @param <Z> The Z3 sort that this const value is represented by.
     * @param <I> The IvySort that this Const is a type of.
     *            TODO: do we actually need the I typevar
     */
    public class IvyConst<J, Z extends Sort> {
        public final String name;
        public final IvySort<J, Z> sort;
        public final Expr<Z> theConst;

        IvyConst(String name, IvySort<J, Z> sort) {
            this.name = name;
            this.sort = sort;
            this.theConst = ctx.mkConst(name, sort.getZ3Sort());
        }

        public Expr<BoolSort> randomize() {
            return ctx.mkEq(theConst, sort.to_solver(sort.fromSupplier()));
        }

        public J eval(Model m) {
            return sort.eval(m, theConst);
        }
    }
}