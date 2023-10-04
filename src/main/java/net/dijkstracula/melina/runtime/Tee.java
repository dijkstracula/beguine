package net.dijkstracula.melina.runtime;

import io.vavr.*;
import net.dijkstracula.melina.actions.Action0;
import net.dijkstracula.melina.actions.Action1;
import net.dijkstracula.melina.actions.Action2;
import net.dijkstracula.melina.history.ActionCall;

import java.util.*;
import java.util.function.Supplier;

// A driver that pipes actions to two protocols rather than just one.
public class Tee<Spec extends Protocol, Impl extends Protocol> extends Driveable<Tuple2<ActionCall, ActionCall>> {

    public final Spec spec;

    public final Impl impl;

    public Tee(MelinaContext ctx, Spec s, Impl i) {
        super(ctx);
        spec = s;
        impl = i;

        Set<String> spec_actions = spec.getActions().keySet();
        Set<String> impl_actions = impl.getActions().keySet();
        if (!spec_actions.equals(impl_actions)) {
            throw new RuntimeException("Mismatch between spec and impl's action sets");
        }
        for (Supplier<Boolean> conj : s.getConjectures()) {
            addConjecture("todo", conj);
        }

        addConjecture("final-histories-match", () -> {
            Tuple2<ActionCall, ActionCall> lastAction = getHistory().get(getHistory().size() - 1);
            System.out.println("final-histories-match: " + lastAction);
            return lastAction._1.equals(lastAction._2);
        });
    }

    protected void addAction(String ident, Supplier<ActionCall> a1, Supplier<ActionCall> a2) {
        Function0<Tuple2<ActionCall, ActionCall>> joined = Action0.join(a1, a2);
        addAction(ident, () -> {
            return joined.apply();
        });
    }

    public <U> void tee0(String ident, Action0<U> spec, Function0<U> impl) {
        addAction(ident, () -> {
            U spec_u = spec.apply();
            U impl_u = impl.apply();

            return new Tuple2<>(
                    ActionCall.fromAction0(ident, spec_u),
                    ActionCall.fromAction0(ident, impl_u));
        });
    }

    public <T, U> void tee1(String ident, Action1<T, U> spec, Function1<T, U> impl, Supplier<T> gen) {
        addAction(ident, () -> {
            Tuple2<T, U> res = spec.genAndApply(gen);
            T t = res._1;
            U spec_u = res._2;

            U impl_u = impl.apply(t);

            return new Tuple2<>(
                    ActionCall.fromAction1(ident, t, spec_u),
                    ActionCall.fromAction1(ident, t, impl_u));
        });
    }

    public <T1, T2, U> void tee2(String ident, Action2<T1, T2, U> spec, Function2<T1, T2, U> impl, Supplier<T1> gen_t1, Supplier<T2> gen_t2) {
        addAction(ident, () -> {
            Tuple3<T1, T2, U> res = spec.genAndApply(gen_t1, gen_t2);
            T1 t1 = res._1;
            T2 t2 = res._2;
            U spec_u = res._3;

            U impl_u = impl.apply(t1, t2);

            return new Tuple2<>(
                    ActionCall.fromAction2(ident, t1, t2, spec_u),
                    ActionCall.fromAction2(ident, t1, t2, impl_u));
        });
    }
}
