package net.dijkstracula.melina.runtime;

import net.dijkstracula.melina.actions.Action0;
import net.dijkstracula.melina.actions.Action1;
import net.dijkstracula.melina.actions.Action3;

import java.util.*;
import java.util.function.Supplier;

/** Abstract class that all Ivy isolates extend. */
public class Protocol extends Driveable<String> {
    public Protocol(Random r) {
        super(r);
    }

    // XXX: shouldn't this be "exportAction"?

    protected <T> void addAction(String ident, Action0<T> r) {
        addAction(ident, () -> {
            T t = r.apply();
            return String.format("%s(%s)", ident, t);
        });
    }

    protected <T, U> void addAction(String ident, Action1<T, U> c, Supplier<T> s) {
        addAction(ident, () -> {
            T t = s.get();
            U u = c.apply(t);
            return String.format("%s(%s, %s)", ident, t, u);
        });
    }

    protected <T1, T2, T3, U> void addAction(String ident, Action3<T1, T2, T3, U> c, Supplier<T1> s1, Supplier<T2> s2, Supplier<T3> s3) {
        addAction(ident, () -> {
            T1 t1 = s1.get();
            T2 t2 = s2.get();
            T3 t3 = s3.get();
            U u = c.apply(t1, t2, t3);
            return String.format("%s(%s, %s, %s, %s)", ident, t1, t2, t3, u);
        });
    }
}
