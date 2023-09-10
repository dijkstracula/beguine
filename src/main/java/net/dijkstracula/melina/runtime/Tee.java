package net.dijkstracula.melina.runtime;

import io.vavr.Function0;
import io.vavr.Tuple2;
import net.dijkstracula.melina.actions.Action0;

import java.util.HashMap;

// A driver that pipes actions to two protocols rather than just one.
public class Tee {

    /** What can be called from the environment */
    private final HashMap<String, Runnable> actions;

    public Tee() {
        actions = new HashMap<>();
    }

    protected <T> void addAction(String ident, Action0<T> a1, Action0<T> a2) {
        Function0<Tuple2<T, T>> joined = Action0.join(a1, a2);
        actions.put(ident, () -> {
            Tuple2<T, T> res = joined.apply();
        });
    }
}
