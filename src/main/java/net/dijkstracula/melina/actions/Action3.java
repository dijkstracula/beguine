package net.dijkstracula.melina.actions;

import io.vavr.Function2;
import io.vavr.Function3;
import io.vavr.Function4;
import io.vavr.Tuple2;
import net.dijkstracula.melina.exceptions.IrvingCodegenException;

import java.util.Optional;


/**
 * An action comprises a function and optional pre/post operations.
 */
public class Action3<T1, T2, T3, U> implements Function3<T1, T2, T3, U> {

    private Optional<Function3<T1, T2, T3, Void>> pre;
    private Function3<T1, T2, T3, U> action;

    private Optional<Function4<T1, T2, T3, U, Void>> post;

    public Action3(Function3<T1, T2, T3, U> f) {
        pre = Optional.empty();
        action = f;
        post = Optional.empty();
    }

    public Action3() {
        pre = Optional.empty();
        action = null;
        post = Optional.empty();
    }

    @Override
    public U apply(T1 t1, T2 t2, T3 t3) {
        if (action == null) {
            throw new IrvingCodegenException.UndefinedActionCall();
        }

        pre.ifPresent(pre -> pre.apply(t1, t2, t3));
        U u = action.apply(t1, t2, t3);
        post.ifPresent(post-> post.apply(t1, t2, t3, u));
        return u;
    }

    public void on(Function3<T1, T2, T3, U> f) {
        if (action != null) {
            throw new IrvingCodegenException.RedefinedActionCall();
        }
        action = f;
    }

    public void before(Function3<T1, T2, T3, Void> f) {
        if (pre.isPresent()) {
            throw new IrvingCodegenException.RedefinedActionCall();
        }
        pre = Optional.of(f);
    }

    public void after(Function4<T1, T2, T3, U, Void> f) {
        if (post.isPresent()) {
            throw new IrvingCodegenException.RedefinedActionCall();
        }
        post = Optional.of(f);
    }

    public static <T1, T2, T3, U> Function3<T1, T2, T3, Tuple2<U, U>> join(Function3<T1, T2, T3, U> spec, Function3<T1, T2, T3, U> impl) {
        return (t1, t2, t3) -> {
            U su = spec.apply(t1, t2, t3);
            U iu = impl.apply(t1, t2, t3);
            return new Tuple2<>(su, iu);
        };
    }
}

