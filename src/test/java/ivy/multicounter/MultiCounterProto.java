package ivy.multicounter;

import com.microsoft.z3.IntSort;
import ivy.decls.Decls;
import ivy.Protocol;
import ivy.sorts.Sorts;

import java.util.Random;


public class MultiCounterProto extends Protocol<MultiCounterRefImpl> {
    public final Decls.IvyConst<Integer, IntSort> node;
    public final Decls.IvyConst<Integer, IntSort> val;

    public MultiCounterProto(Random r, MultiCounterRefImpl impl) {
        super(r);

        Sorts.IvyInt nodeSort = mkInt("nodeSort", 0, impl.max_n);
        node = mkConst("node", nodeSort);
        val = mkConst("val", mkInt("Int"));

        addAction(nodeSort, impl::dec);
        addAction(nodeSort, impl::inc);
        addConjecture("non-negativity", () -> impl.val.values().stream().anyMatch(i -> i < 0));
    }
}
