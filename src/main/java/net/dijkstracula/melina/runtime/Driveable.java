package net.dijkstracula.melina.runtime;

import net.dijkstracula.melina.exceptions.ActionArgGenRetryException;
import net.dijkstracula.melina.exceptions.ConjectureFailure;
import net.dijkstracula.melina.exceptions.GeneratorLivelock;

import java.util.*;
import java.util.function.Supplier;

public abstract class Driveable<T> implements Runnable {
    protected final MelinaContext ctx;

    /** What can be called from the environment */
    private final Map<String, Runnable> actions;
    private final List<String> actionNames;
    private final List<T> history;

    protected final List<Supplier<Boolean>> conjectures;

    public Driveable(MelinaContext c) {
        ctx = c;
        actions = new HashMap<>();
        actionNames = new ArrayList<>();
        history = new ArrayList<>();
        conjectures = new ArrayList<>();
    }


    protected Runnable randomAction() {
        assert(actionNames.size() > 0);
        assert(actions.size() == actionNames.size());

        String aname = ctx.randomSelect(actionNames).get();
        return actions.get(aname);
    }

    public List<T> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public void addDriveable(Driveable<T> p) {
        for (String name : p.getActions().keySet()) {
            addAction(name, p.getActions().get(name));
        }

        for (Supplier<Boolean> conj : p.getConjectures()) {
            addConjecture("TODO", conj);
        }
    }

    protected void addHistory(T t) {
        history.add(t);
    }

    protected void addAction(String name, Runnable action) {
        if (actions.containsKey(name)) {
            throw new RuntimeException("Duplicate action " + name);
        }
        actions.put(name, () -> {
            System.out.println("[Driveable] choosing " + name);
            action.run();
        });
        actionNames.add(name);
    }

    protected void addConjecture(String ident, Supplier<Boolean> conj) {
        conjectures.add(conj);
    }

    protected Map<String, Runnable> getActions() {
        return Collections.unmodifiableMap(actions);
    }

    protected List<Supplier<Boolean>> getConjectures() {
        return Collections.unmodifiableList(conjectures);
    }

    @Override
    public void run() {
        int reattempts = 0;
        while (true) {
            // XXX pull this into a Generator<T>.
            try {
                Runnable action = randomAction();
                action.run();
            } catch (ActionArgGenRetryException e) {
                if (reattempts++ == 20) {
                    throw new GeneratorLivelock();
                }
                System.out.println(String.format("[Driveable] Retrying (reattempt %d)...", reattempts));
                continue;
            }
            break;
        }

        for (Supplier<Boolean> conj : conjectures) {
            if (!conj.get()) {
                throw new ConjectureFailure();
            }
        }
    }
}
