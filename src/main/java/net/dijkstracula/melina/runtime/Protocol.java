package net.dijkstracula.melina.runtime;

import io.vavr.Tuple2;
import net.dijkstracula.melina.actions.Action0;
import net.dijkstracula.melina.actions.Action1;
import net.dijkstracula.melina.actions.Action2;
import net.dijkstracula.melina.actions.Action3;
import net.dijkstracula.melina.history.ActionCall;

import java.util.function.Supplier;

/** Abstract class that all Ivy isolates extend. */
public class Protocol extends Driveable<ActionCall> {
    public Protocol(MelinaContext ctx) {
        super(ctx);
    }

    protected <T> void exportAction(String ident, Action0<T> r) {
        addAction(ident, () -> {
            T t = r.apply();
            return ActionCall.fromAction0(ident, t);
        });
    }

    protected <T, U> void exportAction(String ident, Action1<T, U> c, Supplier<T> s) {
        addAction(ident, () -> {
            Tuple2<T, U> res = c.genAndApply(s);
            return ActionCall.fromAction1(ident, res._1, res._2);
        });
    }

    protected <T1, T2, U> void exportAction(String ident, Action2<T1, T2, U> c, Supplier<T1> s1, Supplier<T2> s2) {
        addAction(ident, () -> {
            T1 t1 = s1.get();
            T2 t2 = s2.get();
            U u = c.apply(t1, t2);
            return ActionCall.fromAction2(ident, t1, t2, u);
        });
    }

    protected <T1, T2, T3, U> void exportAction(String ident, Action3<T1, T2, T3, U> c, Supplier<T1> s1, Supplier<T2> s2, Supplier<T3> s3) {
        addAction(ident, () -> {
            T1 t1 = s1.get();
            T2 t2 = s2.get();
            T3 t3 = s3.get();
            U u = c.apply(t1, t2, t3);
            return ActionCall.fromAction3(ident, t1, t2, t3, u);
        });
    }
}