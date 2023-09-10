package net.dijkstracula.melina.runtime;

import net.dijkstracula.melina.actions.Action0;
import net.dijkstracula.melina.actions.Action1;

import java.util.*;
import java.util.function.Supplier;

/** Abstract class that all Ivy isolates extend. */
public class Protocol {

    /** What can be called from the environment */
    private final HashMap<String, Supplier<String>> actions;

    /** What must be true after any action */
    private final List<Supplier<Boolean>> conjectures;

    public Protocol() {
        actions = new HashMap<>();
        conjectures = new ArrayList<>();
    }

    protected void addAction(String ident, Supplier<String> r) {
        actions.put(ident, r);
    }

    protected <T> void addAction(String ident, Action0<T> r) {
        actions.put(ident, () -> {
            T t = r.apply();
            return String.format("%s(%s)", ident, t);
        });
    }

    protected <T, U> void addAction(String ident, Action1<T, U> c, Supplier<T> s) {
        actions.put(ident, () -> {
            T t = s.get();
            U u = c.apply(t);
            return String.format("%s(%s, %s)", ident, t, u);
        });
    }

    protected void addConjecture(Supplier<Boolean> pred) {
        conjectures.add(pred);
    }

    public Map<String, Supplier<String>> getActions() {
        // XXX: this is dumb.
        return Collections.unmodifiableMap(actions);
    }

    public List<Supplier<Boolean>> getConjectures() {
        return Collections.unmodifiableList(conjectures);
    }
}
