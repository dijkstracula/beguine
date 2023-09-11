package net.dijkstracula.melina.runtime;

import net.dijkstracula.melina.exceptions.ConjectureFailureException;

import java.util.*;
import java.util.function.Supplier;

public abstract class Driveable<T> implements Runnable {
    private final Random random;

    /** What can be called from the environment */
    private final Map<String, Supplier<T>> actions;
    private final List<String> actionNames;
    private final List<T> history;

    private final List<Supplier<Boolean>> conjectures;

    public Driveable(Random r) {
        random = r;
        actions = new HashMap<>();
        actionNames = new ArrayList<>();
        history = new ArrayList<>();
        conjectures = new ArrayList<>();
    }


    protected Supplier<T> randomAction() {
        assert(actionNames.size() > 0);
        String aname = actionNames.get(random.nextInt(actionNames.size()));
        return actions.get(aname);
    }

    public List<T> getHistory() {
        return Collections.unmodifiableList(history);
    }

    protected void addHistory(T t) {
        history.add(t);
    }

    protected void addAction(String name, Supplier<T> action) {
        actions.put(name, action);
        actionNames.add(name);
    }

    protected void addConjecture(Supplier<Boolean> conj) {
        conjectures.add(conj);
    }

    @Override
    public void run() {
        assert(actions.size() > 0);
        String act = actionNames.get(random.nextInt(actions.size()));
        T t = actions.get(act).get();
        addHistory(t);

        for (Supplier<Boolean> conj : conjectures) {
            if (!conj.get()) {
                throw new ConjectureFailureException();
            }
        }
    }
}
