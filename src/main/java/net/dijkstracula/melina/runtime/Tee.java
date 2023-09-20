package net.dijkstracula.melina.runtime;

import io.vavr.Function0;
import io.vavr.Tuple2;
import net.dijkstracula.melina.actions.Action0;

import java.util.*;
import java.util.function.Supplier;

// A driver that pipes actions to two protocols rather than just one.
public class Tee<Spec extends Protocol, Impl extends Protocol> extends Driveable<Tuple2<String, String>> {

    private final Spec spec;

    private final Impl impl;

    public Tee(Random r, Spec s, Impl i) {
        super(r);
        spec = s;
        impl = i;

        Set<String> spec_actions = spec.getActions().keySet();
        Set<String> impl_actions = impl.getActions().keySet();
        if (!spec_actions.equals(impl_actions)) {
            throw new RuntimeException("Mismatch between spec and impl's action sets");
        }
        for (String aname : spec_actions) {
            addAction(aname, s.getActions().get(aname), i.getActions().get(aname));
        }
        for (Supplier<Boolean> conj : s.getConjectures()) {
            addConjecture("todo", conj);
        }

        addConjecture("final-histories-match", () -> {
            Tuple2<String, String> lastAction = getHistory().get(getHistory().size() - 1);
            System.out.println("final-histories-match: " + lastAction);
            return lastAction._1.equals(lastAction._2);
        });
    }

    protected void addAction(String ident, Supplier<String> a1, Supplier<String> a2) {
        Function0<Tuple2<String, String>> joined = Action0.join(a1, a2);
        addAction(ident, () -> {
            return joined.apply();
        });
    }
}
