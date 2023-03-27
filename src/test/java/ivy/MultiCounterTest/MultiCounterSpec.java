package ivy.MultiCounterTest;

import com.microsoft.z3.IntSort;
import ivy.Conjecture;
import ivy.decls.Decls;
import ivy.Specification;
import ivy.sorts.Sorts;

import java.util.Random;


public class MultiCounterSpec extends Specification<MultiCounterRefImpl> {
    public final Decls.IvyConst<Integer, IntSort> node;
    public final Decls.IvyConst<Integer, IntSort> val;

    public MultiCounterSpec(Random r, MultiCounterRefImpl impl) {
        super(r, impl);

        Sorts.IvyInt nodeSort = mkInt("nodeSort", 0, impl.max_n);
        node = mkConst("node", nodeSort);
        val = mkConst("val", mkInt("Int"));

        addAction(nodeSort, impl::dec);
        addAction(nodeSort, impl::inc);
        addConjecture("non-negativity", (im) -> {
            for (int i : im.val.values()) {
                if (i < 0) return false;
            }
            return true;
        });
    }

}
