package net.dijkstracula.melina.runtime;

import net.dijkstracula.melina.exceptions.ActionArgGenRetryException;
import net.dijkstracula.melina.exceptions.ConjectureFailureException;

import java.util.*;
import java.util.function.Supplier;

public abstract class Driveable<T> implements Runnable {
    protected final MelinaContext ctx;

    /** What can be called from the environment */
    private final Map<String, Supplier<T>> actions;
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


    protected Supplier<T> randomAction() {
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

    protected void addAction(String name, Supplier<T> action) {
        if (actions.containsKey(name)) {
            throw new RuntimeException("Duplicate action " + name);
        }
        actions.put(name, () -> {
            System.out.println("[Driveable] choosing " + name);
            return action.get();
        });
        actionNames.add(name);
    }

    protected void addConjecture(String ident, Supplier<Boolean> conj) {
        conjectures.add(conj);
    }

    protected Map<String, Supplier<T>> getActions() {
        return Collections.unmodifiableMap(actions);
    }

    protected List<Supplier<Boolean>> getConjectures() {
        return Collections.unmodifiableList(conjectures);
    }

    @Override
    public void run() {
        T t;
        int reattempts = 0;
        while (true) {
            try {
                Supplier<T> action = randomAction();
                t = action.get();
            } catch (ActionArgGenRetryException e) {
                if (reattempts++ == 20) {
                    throw new RuntimeException("Too many retries!");
                }
                System.out.println(String.format("[Driveable] Retrying (reattempt %d)...", reattempts));
                continue;
            }
            addHistory(t);
            break;
        }

        for (Supplier<Boolean> conj : conjectures) {
            if (!conj.get()) {
                throw new ConjectureFailureException();
            }
        }
    }
}
