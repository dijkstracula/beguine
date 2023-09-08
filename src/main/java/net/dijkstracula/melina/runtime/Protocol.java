package net.dijkstracula.melina.runtime;

import net.dijkstracula.melina.actions.Action0;
import net.dijkstracula.melina.actions.Action1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** Abstract class that all Ivy isolates extend. */
public class Protocol {

    /** What can be called from the environment */
    private final HashMap<String, Runnable> actions;

    /** What must be true after any action */
    private final List<Supplier<Boolean>> conjectures;

    /** What actions have been taken?
     * TODO: Should be something better-structured than just a string!
     * */
    private final List<String> history;

    public Protocol() {
        actions = new HashMap<>();
        conjectures = new ArrayList<>();
        history = new ArrayList<>();
    }

    protected void addAction(String ident, Runnable r) {
        actions.put(ident, r);
    }

    protected <T> void addAction(String ident, Action0<T> r) {
        actions.put(ident, () -> {
            T t = r.apply();
            history.add(String.format("%s(%s)", ident, t));
        });
    }

    protected <T, U> void addAction(String ident, Action1<T, U> c, Supplier<T> s) {
        actions.put(ident, () -> {
            T t = s.get();
            U u = c.apply(t);
            history.add(String.format("%s(%s, %s)", ident, t, u));
        });
    }

    protected void addConjecture(Supplier<Boolean> pred) {
        conjectures.add(pred);
    }

    public List<Runnable> getActions() {
        // XXX: this is dumb.
        return actions.values().stream().collect(Collectors.toList());
    }

    public List<Supplier<Boolean>> getConjectures() {
        return Collections.unmodifiableList(conjectures);
    }

    public List<String> getHistory() {
        return Collections.unmodifiableList(history);
    }
}
