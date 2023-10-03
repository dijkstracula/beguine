package net.dijkstracula.irving.sorts;

import com.microsoft.z3.BoolSort;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Sort;
import net.dijkstracula.melina.runtime.MelinaContext;

import java.util.function.Function;
import java.util.function.Supplier;


/**
 * A proxy class that connects the runtime JVM type J with the Z3 solver sort Z.
 *
 * @param <J> The Java datatype representing this type
 * @param <Z> The Z3 sort representing this type
 */
public abstract class IvySort<J, Z extends Sort> {

    @FunctionalInterface
    interface Constraint<Z extends Sort> extends Function<Expr<Z>, Expr<BoolSort>> {
        public Expr<BoolSort> apply(Expr<Z> zExpr);
    }

    /** The name of the sort from the extracted Ivy code. */
    protected final String name;

    protected final MelinaContext ctx;

    public IvySort(String name, MelinaContext ctx) {
        this.name = name;
        this.ctx = ctx;
    }

    /**
     * Instantiates a nascent instance of this sort.
     * @return
     */
    public abstract J make();

    /**
     * Produces a supplier that generates a random instance of this sort.
     * @return
     */
    public abstract Supplier<J> generator();

    /**
     * Transforms an instance of the given sort into its Z3 representation.
     */
    public abstract Expr<Z> to_solver(J val);

    /**
     * Produces the underlying Z3 sort used by the solver.
     */
    public abstract Z getZ3Sort();
}