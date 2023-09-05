package net.dijkstracula.melina.actions;

import java.util.Optional;

import io.vavr.Function1;
import io.vavr.Function2;
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
}

