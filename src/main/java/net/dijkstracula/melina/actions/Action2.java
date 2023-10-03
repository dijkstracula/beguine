package net.dijkstracula.melina.actions;

import io.vavr.Function2;
import io.vavr.Function3;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import net.dijkstracula.melina.exceptions.ActionArgGenRetryException;
import net.dijkstracula.melina.exceptions.GeneratorLivelock;
import net.dijkstracula.melina.exceptions.IrvingCodegenException;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * An action comprises a function and optional pre/post operations.
 */
public class Action2<T1, T2, U> implements Function2<T1, T2, U> {

    private Optional<Function2<T1,T2, Void>> pre;
    private Function2<T1,T2, U> action;

    private Optional<Function3<T1, T2, U, Void>> post;

    public Action2(Function2<T1, T2, U> f) {
        pre = Optional.empty();
        action = f;
        post = Optional.empty();
    }

    public Action2() {
        pre = Optional.empty();
        action = null;
        post = Optional.empty();
    }

    @Override
    public U apply(T1 t1, T2 t2) {
        if (action == null) {
            throw new IrvingCodegenException.UndefinedActionCall();
        }

        pre.ifPresent(pre -> pre.apply(t1, t2));
        U u = action.apply(t1, t2);
        post.ifPresent(post-> post.apply(t1, t2, u));
        return u;
    }

    public Tuple3<T1, T2, U> genAndApply(Supplier<T1> gen_t1, Supplier<T2> gen_t2) throws GeneratorLivelock {
        int reattempts = 0;
        while (true) {
            // XXX pull this into a Generator<T>.
            try {
                T1 t1 = gen_t1.get();
                T2 t2 = gen_t2.get();
                return new Tuple3<>(t1, t2, apply(t1, t2));
            } catch (ActionArgGenRetryException e) {
                if (reattempts++ == 20) {
                    throw new GeneratorLivelock();
                }
                System.out.println(String.format("[Action1] Retrying (reattempt %d)...", reattempts));
            }
        }
    }

    public void on(Function2<T1, T2, U> f) {
        if (action != null) {
            throw new IrvingCodegenException.RedefinedActionCall();
        }
        action = f;
    }

    public void before(Function2<T1, T2, Void> f) {
        if (pre.isPresent()) {
            throw new IrvingCodegenException.RedefinedActionCall();
        }
        pre = Optional.of(f);
    }

    public void after(Function3<T1, T2, U, Void> f) {
        if (post.isPresent()) {
            throw new IrvingCodegenException.RedefinedActionCall();
        }
        post = Optional.of(f);
    }

    public static <T1, T2, U> Function2<T1, T2, Tuple2<U, U>> join(Function2<T1, T2, U> spec, Function2<T1, T2, U> impl) {
        return (t1, t2) -> {
            U su = spec.apply(t1, t2);
            U iu = impl.apply(t1, t2);
            return new Tuple2<>(su, iu);
        };
    }
}

