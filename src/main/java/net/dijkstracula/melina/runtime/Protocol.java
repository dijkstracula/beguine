package net.dijkstracula.melina.runtime;

import net.dijkstracula.melina.actions.Action0;
import net.dijkstracula.melina.actions.Action1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/** Abstract class that all Ivy isolates extend. */
public class Protocol {

    /** What can be called from the environment */
    private final List<Runnable> actions;

    /** What must be true after any action */
    private final List<Supplier<Boolean>> conjectures;

    /** What actions have been taken?
     * TODO: Should be something better-structured than just a string!
     * */
    private final List<String> history;

    public Protocol() {
        actions = new ArrayList<>();
        conjectures = new ArrayList<>();
        history = new ArrayList<>();
    }

    protected void addAction(Runnable r) {
        actions.add(r);
    }

    protected <T> void addAction(String ident, Action0<T> r) {
        actions.add(() -> {
            T t = r.apply();
            history.add(String.format("%s(%s)", ident, t));
        });
    }

    protected <T, U> void addAction(String ident, Action1<T, U> c, Supplier<T> s) {
        addAction(() -> {
            T t = s.get();
            U u = c.apply(t);
            history.add(String.format("%s(%s, %s)", ident));
        });
    }

    protected void addConjecture(Supplier<Boolean> pred) {
        conjectures.add(pred);
    }

    public List<Runnable> getActions() {
        return Collections.unmodifiableList(actions);
    }

    public List<Supplier<Boolean>> getConjectures() {
        return Collections.unmodifiableList(conjectures);
    }

    public List<String> getHistory() {
        return Collections.unmodifiableList(history);
    }
}
