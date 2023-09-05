package net.dijkstracula.irving.sorts;

import com.microsoft.z3.BoolSort;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Model;
import com.microsoft.z3.Sort;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * A proxy class that connects the runtime JVM type J with the Z3 solver sort Z.
 *
 * @param <J> The Java datatype representing this type
 * @param <Z> The Z3 sort representing this type
 */
public abstract class IvySort<J, Z extends Sort> implements Supplier<J> {

    @FunctionalInterface
    interface Constraint<Z extends Sort> extends Function<Expr<Z>, Expr<BoolSort>> {
        public Expr<BoolSort> apply(Expr<Z> zExpr);
    }

    /** The name of the sort from the extracted Ivy code. */
    protected final String name;

    /** Produces a random value of type J that conforms to the sort's constraints. */
    protected final Supplier<J> supplier;

    public IvySort(String name, Supplier<J> f) {
        this.name = name;
        this.supplier = f;
    }

    /** Produce a random value of type J from the sort's supplier. */
    public J get() {
        return supplier.get();
    }

    /** Transforms an instance of the given sort into its Z3 representation. */
    public abstract Expr<Z> to_solver(J val);

    /** Produces the underlying Z3 sort used by the solver. */
    public abstract Z getZ3Sort();
}