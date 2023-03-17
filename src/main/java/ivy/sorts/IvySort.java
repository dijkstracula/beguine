package ivy.sorts;

import com.microsoft.z3.BoolSort;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Model;
import com.microsoft.z3.Sort;

import java.util.List;
import java.util.function.Supplier;

public abstract class IvySort<J, Z extends Sort> {
    @FunctionalInterface
    interface Constraint<J, Z extends Sort> {
        public Expr<BoolSort> apply(Expr<Z> zExpr);
    }

    protected final String name;
    protected final Supplier<J> supplier;
    protected final List<Constraint<J, Z>> constraints;

    public IvySort(String name, Supplier<J> f, Constraint<J, Z>... constraints) {
        this.name = name;
        this.supplier = f;
        this.constraints = List.of(constraints);
    }

    public J fromSupplier() {
        return supplier.get();
    }

    public abstract Expr<Z> to_solver(J val);

    public abstract Z getZ3Sort();

    // TODO: should a Sort just hold a pointer back its Model (or even the Gen?)
    public abstract J eval(Model m, Expr<Z> e);

}