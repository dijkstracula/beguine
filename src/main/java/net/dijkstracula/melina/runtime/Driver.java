package net.dijkstracula.melina.runtime;

import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import net.dijkstracula.irving.sorts.Sorts;
import net.dijkstracula.melina.exceptions.ActionArgGenRetryException;
import net.dijkstracula.melina.exceptions.ConjectureFailureException;
import net.dijkstracula.melina.exceptions.MelinaException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Top-level class for choosing actions to take and validating global invariants.
 */
public class Driver<P extends Protocol> implements Runnable {
    protected final Random random;

    private final Context ctx;
    private final Solver slvr;

    protected final Sorts sorts;

    private final P protocol;

    // XXX: When we consume the P, we do the following to all the actions:
    // 1) Mutate the the action to have a custom post that updates the history
    private final Map<String, Supplier<String>> actions;
    private final List<String> actionNames;

    private final List<String> history;

    public Driver(Random r, P p) {
        random = r;
        ctx = new Context();
        slvr = ctx.mkSolver();
        sorts = new Sorts(ctx, r);

        protocol = p;
        actions = p.getActions();
        actionNames = actions.keySet().stream().collect(Collectors.toList());

        history = new ArrayList<>();
    }

    @Override
    public void run() throws MelinaException {
        assert(actions.size() > 0);
        String res;
        while (true) {
            // TODO: track retry counts, other metrics?
            try {
                res = randomAction().get();
                break;
            } catch (ActionArgGenRetryException e) { /* retry */ }
        }
        history.add(res);
        for (Supplier<Boolean> conj : protocol.getConjectures()) {
            if (conj.get() == false) {
                throw new ConjectureFailureException();
            }
        }
    }

    public P getProtocol() {
         return protocol;
    }

    private Supplier<String> randomAction() {
        String aname = actionNames.get(random.nextInt(actionNames.size()));
        return actions.get(aname);
    }
}
