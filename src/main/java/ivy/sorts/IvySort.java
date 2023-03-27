package ivy.sorts;

import com.microsoft.z3.BoolSort;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Model;
import com.microsoft.z3.Sort;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class IvySort<J, Z extends Sort> implements Supplier<J> {

    @FunctionalInterface
    interface Constraint<Z extends Sort> extends Function<Expr<Z>, Expr<BoolSort>> {
        public Expr<BoolSort> apply(Expr<Z> zExpr);
    }

    protected final String name;
    protected final Supplier<J> supplier;
    protected final List<Constraint<Z>> constraints;

    public IvySort(String name, Supplier<J> f, Constraint<Z>... constraints) {
        this.name = name;
        this.supplier = f;
        this.constraints = List.of(constraints);
    }

    public J get() {
        return supplier.get();
    }

    public abstract Expr<Z> to_solver(J val);

    public abstract Z getZ3Sort();

    // TODO: should a Sort just hold a pointer back its Model (or even the Gen?)
    public abstract J eval(Model m, Expr<Z> e);

}