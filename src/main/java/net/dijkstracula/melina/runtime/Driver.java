package net.dijkstracula.melina.runtime;

import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import net.dijkstracula.irving.sorts.Sorts;
import net.dijkstracula.melina.actions.Action0;
import net.dijkstracula.melina.exceptions.ActionArgGenRetryException;
import net.dijkstracula.melina.exceptions.ConjectureFailureException;
import net.dijkstracula.melina.exceptions.MelinaException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Top-level class for choosing actions to take.
 * TODO: for n-way implementation tests, does that live here or in something higher-up?
 */
public class Driver implements Runnable {
    protected final Random random;

    private final Context ctx;
    private final Solver slvr;

    protected final Sorts sorts;

    private List<Supplier<Boolean>> conjectures;

    private final List<Runnable> actions;

    public Driver(Random r) {
        random = r;
        ctx = new Context();
        slvr = ctx.mkSolver();
        sorts = new Sorts(ctx, r);

        actions = new ArrayList<>();
        conjectures = new ArrayList<>();
    }

    public void addProtocol(Protocol p) {
        actions.addAll(p.getActions());
        conjectures.addAll(p.getConjectures());
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
