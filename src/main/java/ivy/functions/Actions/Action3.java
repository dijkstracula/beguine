package ivy.functions.Actions;

import io.vavr.Function1;
import io.vavr.Function3;
import io.vavr.Tuple3;
import io.vavr.control.Either;
import ivy.exceptions.IvyExceptions.ActionException;

import java.util.function.Supplier;

/**
 * An action comprises a function and optional pre/post operations.
 */
public class Action3<T1, T2, T3, U> implements Function3<T1, T2, T3, Either<ActionException, U>> {
    private final Function3<T1, T2, T3, Either<ActionException, U>> action;


    private Action3(Function3<T1, T2, T3, Either<ActionException, U>> f) {
        action = f;
    }

    @Override
    public Either<ActionException, U> apply(T1 t1, T2 t2, T3 t3) {
        return action.apply(t1, t2, t3);
    }

    //

    public static <T1, T2, T3, U> Action3<T1, T2, T3, U> from(Function3<T1, T2, T3, Either<ActionException, Void>> pre,
                                                              Function3<T1, T2, T3, U> f,
                                                              Function1<U, Either<ActionException, U>> post) {
        return new Action3<>((t1, t2, t3) ->
                pre.apply(t1, t2, t3)
                        .map(v -> f.apply(t1, t2, t3))
                        .flatMap(post));
    }

    public static <T1, T2, T3, U> Action3<T1, T2, T3, U> from(Function3<T1, T2, T3, Either<ActionException, Void>> pre,
                                                              Function3<T1, T2, T3, U> f) {
        return new Action3<>((t1, t2, t3) ->
                pre.apply(t1, t2, t3)
                        .map(v -> f.apply(t1, t2, t3)));
    }

    public static <T1, T2, T3, U> Action3<T1, T2, T3, U> from(Function3<T1, T2, T3, U> f) {
        return new Action3<>((t1, t2, t3) -> Either.right(f.apply(t1, t2, t3)));
    }

    public Action0<U> pipe(Supplier<Tuple3<T1, T2, T3>> source) {
        return new Action0<U>("TODO", () -> source.get().apply(this));
    }
}
