package net.dijkstracula.melina.actions;

import java.util.Optional;
import java.util.function.Supplier;

import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Tuple2;
import net.dijkstracula.melina.exceptions.ActionArgGenRetryException;
import net.dijkstracula.melina.exceptions.GeneratorLivelock;
import net.dijkstracula.melina.exceptions.IrvingCodegenException;

/**
 * An action comprises a function and optional pre/post operations.
 */
public class Action1<T, U> implements Function1<T, U> {

    private Optional<Function1<T, Void>> pre;
    private Function1<T, U> action;

    private Optional<Function2<T, U, Void>> post;

    public Action1(Function1<T, U> f) {
        pre = Optional.empty();
        action = f;
        post = Optional.empty();
    }

    public Action1() {
        pre = Optional.empty();
        action = null;
        post = Optional.empty();
    }

    @Override
    public U apply(T t) {
        if (action == null) {
            throw new IrvingCodegenException.UndefinedActionCall();
        }

        pre.ifPresent(pre -> pre.apply(t));
        U u = action.apply(t);
        post.ifPresent(post-> post.apply(t, u));
        return u;
    }

    public Tuple2<T, U> genAndApply(Supplier<T> gen_t) throws GeneratorLivelock {
        int reattempts = 0;
        while (true) {
            // XXX pull this into a Generator<T>.
            try {
                T t = gen_t.get();
                return new Tuple2<>(t, apply(t));
            } catch (ActionArgGenRetryException e) {
                if (reattempts++ == 5) {
                    throw e;
                }
                System.out.println(String.format("[Action1] Retrying (reattempt %d)...", reattempts));
            }
        }
    }

    public void on(Function1<T, U> f) {
        if (action != null) {
            throw new IrvingCodegenException.RedefinedActionCall();
        }
        action = f;
    }

    public void before(Function1<T, Void> f) {
        if (pre.isPresent()) {
            throw new IrvingCodegenException.RedefinedActionCall();
        }
        pre = Optional.of(f);
    }

    public void after(Function2<T, U, Void> f) {
        if (post.isPresent()) {
            throw new IrvingCodegenException.RedefinedActionCall();
        }
        post = Optional.of(f);
    }

    public static <T, U> Function1<T, Tuple2<U, U>> join(Function1<T, U> spec, Function1<T, U> impl) {
        return (t) -> {
            U su = spec.apply(t);
            U iu = impl.apply(t);
            return new Tuple2<>(su, iu);
        };
    }
}

