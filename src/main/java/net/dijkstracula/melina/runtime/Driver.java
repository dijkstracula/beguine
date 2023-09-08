package net.dijkstracula.melina.runtime;

import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import net.dijkstracula.irving.sorts.Sorts;
import net.dijkstracula.melina.exceptions.ActionArgGenRetryException;
import net.dijkstracula.melina.exceptions.ConjectureFailureException;
import net.dijkstracula.melina.exceptions.MelinaException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Top-level class for choosing actions to take and validating global invariants.
 */
public class Driver implements Runnable {
    protected final Random random;

    private final Context ctx;
    private final Solver slvr;

    protected final Sorts sorts;

    private final List<Runnable> actions;

    private final List<Supplier<Boolean>> conjectures;


    public Driver(Random r, Protocol p) {
        random = r;
        ctx = new Context();
        slvr = ctx.mkSolver();
        sorts = new Sorts(ctx, r);

        actions = p.getActions();
        conjectures = p.getConjectures();
    }

    @Override
    public void run() throws MelinaException {
        assert(actions.size() > 0);
        while (true) {
            // TODO: track retry counts, other metrics?
            try {
                actions.get(random.nextInt(actions.size())).run();
                break;
            } catch (ActionArgGenRetryException e) { /* retry */ }
        }
        for (Supplier<Boolean> conj : conjectures) {
            if (conj.get() == false) {
                throw new ConjectureFailureException();
            }
        }
    }
}
