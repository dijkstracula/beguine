package ivy.functions.Actions;

import io.vavr.Function0;
import io.vavr.Function1;
import io.vavr.control.Either;
import ivy.exceptions.IvyExceptions.ActionException;

/**
 * An action comprises a function and optional pre/post operations.
 */
public class Action0<U> implements Function0<Either<ActionException, U>> {
    private final Function0<Either<ActionException, U>> action;


    Action0(Function0<Either<ActionException, U>> f) {
        action = f;
    }

    public static <U> Action0<U> from(Function0<Either<ActionException, Void>> pre,
                                      Function0<U> f,
                                      Function1<U, Either<ActionException, U>> post) {
        return new Action0<>(
                () -> pre.get()
                .map(v -> f.apply())
                .flatMap(post));
    }

    public static <U> Action0<U> from(Function0<Either<ActionException, Void>> pre,
                                      Function0<U> f) {
        return new Action0<>(() -> pre.get().map(v -> f.apply()));
    }


    public static <U> Action0<U> from(Function0<U> f) {
        return new Action0<>(() -> Either.right(f.apply()));
    }

    public static <U> Action0<U> from(Runnable f) {
        return new Action0<>(() -> {
            f.run();
            return Either.right(null);
        });
    }

    @Override
    public Either<ActionException, U> apply() {
        return action.apply();
    }

    public Function0<Either<ActionException, U>> pipe(Runnable sideEffect) {
        return () -> {
            sideEffect.run();
            return action.apply();
        };
    }
}
