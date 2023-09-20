package net.dijkstracula.melina.runtime;

import net.dijkstracula.melina.exceptions.ActionArgGenRetryException;
import net.dijkstracula.melina.exceptions.ConjectureFailureException;

import java.util.*;
import java.util.function.Supplier;

public abstract class Driveable<T> implements Runnable {
    protected final Random random;

    /** What can be called from the environment */
    private final Map<String, Supplier<T>> actions;
    private final List<String> actionNames;
    private final List<T> history;

    protected final List<Supplier<Boolean>> conjectures;

    public Driveable(Random r) {
        random = r;
        actions = new HashMap<>();
        actionNames = new ArrayList<>();
        history = new ArrayList<>();
        conjectures = new ArrayList<>();
    }


    protected Supplier<T> randomAction() {
        assert(actionNames.size() > 0);
        assert(actions.size() == actionNames.size());

        String aname = actionNames.get(random.nextInt(actionNames.size()));
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
        while (true) {
            try {
                Supplier<T> action = randomAction();
                t = action.get();
            } catch (ActionArgGenRetryException e) {
                System.out.println("[Driveable] Retrying action gen");
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
