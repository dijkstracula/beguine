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

    public Protocol() {
        actions = new ArrayList<>();
        conjectures = new ArrayList<>();
    }

    protected void addAction(Runnable r) {
        actions.add(r);
    }

    protected void addAction(Action0<Void> r) {
        actions.add(() -> r.apply());
    }

    protected <T> void addAction(Action1<T, Void> c, Supplier<T> s) {
        addAction(() -> c.apply(s.get()));
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
}
