package ivy.functions.Actions;

import io.vavr.Function1;
import io.vavr.control.Either;
import ivy.exceptions.IvyExceptions.ActionException;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An action comprises a function and optional pre/post operations.
 */
public class Action1<T, U> implements Function1<T, Either<ActionException, U>> {
    private Function1<T, Either<ActionException, U>> action;


    public Action1(Function1<T, Either<ActionException, U>> f) {
        action = f;
    }

    public Action1() {
        action = null;
    }

    @Override
    public Either<ActionException, U> apply(T t) {
        if (action == null) {
            throw new RuntimeException("Action was never defined!");
        }
        return action.apply(t);
    }

    public void on(Function1<T, Either<ActionException, U>> f) {
        if (action == null) {
            throw new RuntimeException("Action was already defined!");
        }
        action = f;
    }

    //

    public static <T, U> Action1<T, U> from(Function1<T, Either<ActionException, Void>> pre,
                                            Function1<T, U> f,
                                            Function1<U, Either<ActionException, U>> post) {
        return new Action1<>((t) ->
                pre.apply(t)
                        .map(v -> f.apply(t))
                        .flatMap(post));
    }

    public static <T, U> Action1<T, U> from(Function1<T, Either<ActionException, Void>> pre,
                                            Function1<T, U> f) {
        return new Action1<>((t) ->
                pre.apply(t)
                        .map(v -> f.apply(t)));
    }


    public static <T, U> Action1<T, U> from(Function1<T, U> f) {
        return new Action1<>((t) -> Either.right(f.apply(t)));
    }

    public static <T> Action1<T, Void> from(Consumer<T> f) {
        return new Action1<>((t) -> {
            f.accept(t);
            return Either.right(null);
        });
    }

    public Action0<U> pipe(Supplier<T> source) {
        return new Action0<U>("TODO", () -> this.apply(source.get()));
    }
}
