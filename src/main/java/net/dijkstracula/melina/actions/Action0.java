package net.dijkstracula.melina.actions;

import io.vavr.Function0;
import io.vavr.Function1;
import io.vavr.Tuple2;
import net.dijkstracula.melina.exceptions.ActionArgGenRetryException;
import net.dijkstracula.melina.exceptions.IrvingCodegenException;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * An action comprises a function and optional pre/post operations.
 */
public class Action0<U> implements Function0<U> {

    private Optional<Function0<Void>> pre;
    private Function0<U> action;

    private Optional<Function1<U, Void>> post;

    public Action0(Function0<U> f) {
        pre = Optional.empty();
        action = f;
        post = Optional.empty();
    }

    public Action0() {
        pre = Optional.empty();
        action = null;
        post = Optional.empty();
    }

    @Override
    public U apply() {
        if (action == null) {
            throw new IrvingCodegenException.UndefinedActionCall();
        }

        pre.ifPresent(Function0::apply);
        U u = action.apply();
        post.ifPresent(post -> post.apply(u));
        return u;
    }

    public void on(Function0<U> f) {
        if (action != null) {
            throw new IrvingCodegenException.RedefinedActionCall();
        }
        action = f;
    }

    public void before(Function0<Void> f) {
        if (pre.isPresent()) {
            throw new IrvingCodegenException.RedefinedActionCall();
        }
        pre = Optional.of(f);
    }

    public void after(Function1<U,Void> f) {
        if (post.isPresent()) {
            throw new IrvingCodegenException.RedefinedActionCall();
        }
        post = Optional.of(f);
    }

    public static <U> Function0<Tuple2<U, U>> join(Supplier<U> spec, Supplier<U> impl) {
        return () -> {
            U su;
            U iu;
            int reattempts;

            System.out.println("[Tee] Executing spec");
            reattempts = 0;
            while (true) {
                try {
                    su = spec.get();
                } catch (ActionArgGenRetryException e) {
                    if (reattempts++ == 20) {
                        throw new RuntimeException("Too many retries!");
                    }
                    System.out.println(String.format("[Driveable] Retrying impl (reattempt %d)...", reattempts));
                    continue;
                }
                break;
            }

            System.out.println("[Tee] Executing impl");
            reattempts = 0;
            while (true) {
                try {
                    iu = impl.get();
                } catch (ActionArgGenRetryException e) {
                    if (reattempts++ == 20) {
                        throw new RuntimeException("Too many retries!");
                    }
                    System.out.println(String.format("[Driveable] Retrying impl (reattempt %d)...", reattempts));
                    continue;
                }
                break;
            }

            System.out.println("[Tee] Done");
            return new Tuple2<>(su, iu);
        };
    }
}
