package ivy.functions.Actions;

import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import ivy.exceptions.IvyExceptions.ActionException;

import java.util.function.Supplier;

/**
 * An action comprises a function and optional pre/post operations.
 */
public class Action2<T1, T2, U> implements Function2<T1, T2, Either<ActionException, U>> {
    private final Function2<T1, T2, Either<ActionException, U>> action;


    private Action2(Function2<T1, T2, Either<ActionException, U>> f) {
        action = f;
    }

    @Override
    public Either<ActionException, U> apply(T1 t1, T2 t2) {
        return action.apply(t1, t2);
    }

    //

    public static <T1, T2, U> Action2<T1, T2, U> from(Function2<T1, T2, Either<ActionException, Void>> pre,
                                                      Function2<T1, T2, U> f,
                                                      Function1<U, Either<ActionException, U>> post) {
        return new Action2<>((t1, t2) ->
                pre.apply(t1, t2)
                        .map(v -> f.apply(t1, t2))
                        .flatMap(post));
    }

    public static <T1, T2, U> Action2<T1, T2, U> from(Function2<T1, T2, Either<ActionException, Void>> pre,
                                                      Function2<T1, T2, U> f) {
        return new Action2<>((t1, t2) ->
                pre.apply(t1, t2)
                        .map(v -> f.apply(t1, t2)));
    }

    public static <T1, T2, U> Action2<T1, T2, U> from(Function2<T1, T2, U> f) {
        return new Action2<>((t1, t2) -> Either.right(f.apply(t1, t2)));
    }

    public Action0<U> pipe(Supplier<Tuple2<T1, T2>> source) {
        return new Action0<U>("TODO", () -> source.get().apply(this));
    }
}
