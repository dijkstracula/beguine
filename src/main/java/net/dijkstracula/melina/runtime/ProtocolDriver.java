package net.dijkstracula.melina.runtime;

import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import io.vavr.Tuple2;
import net.dijkstracula.irving.sorts.Sorts;
import net.dijkstracula.melina.exceptions.ConjectureFailureException;
import net.dijkstracula.melina.exceptions.MelinaException;

import java.util.Random;
import java.util.function.Supplier;

/**
 * Top-level class for choosing actions to take and validating global invariants on a
 * single Protocol.
 */
public class ProtocolDriver<P extends Protocol> extends Driveable<String> implements Runnable {
    protected final Random random;

    private final Context ctx;
    private final Solver slvr;

    protected final Sorts sorts;

    private final P protocol;

    public ProtocolDriver(Random r, P p) {
        super(r);
        random = r;
        ctx = new Context();
        slvr = ctx.mkSolver();
        sorts = new Sorts(ctx, r);

        protocol = p;
        for (String name : p.getActions().keySet()) {
            addAction(name, p.getActions().get(name));
        }

        for (Supplier<Boolean> conj : p.getConjectures()) {
            addConjecture(conj);
        }
    }

    public P getProtocol() {
         return protocol;
    }
}
