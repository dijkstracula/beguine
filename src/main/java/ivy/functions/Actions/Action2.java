package ivy.functions.Actions;

import io.vavr.Function0;
import io.vavr.Function2;
import io.vavr.Function3;
import io.vavr.control.Either;
import ivy.exceptions.IvyExceptions.ActionException;

import java.util.Optional;

/**
 * An action comprises a function and optional pre/post operations.
 */
public class Action2<T1, T2, U> implements Function2<T1, T2, Either<ActionException, U>> {
    private final Optional<Function0<Optional<ActionException>>> pre;
    private final Function2<T1, T2, U> action;
    private final Optional<Function3<T1, T2, U, Optional<ActionException>>> post;

    public Action2(Optional<Function0<Optional<ActionException>>> pre,
                   Function2<T1, T2, U> f,
                   Optional<Function3<T1, T2, U, Optional<ActionException>>> post) {
        this.pre = pre;
        this.action = f;
        this.post = post;
    }

    @Override
    public Either<ActionException, U> apply(T1 t1, T2 t2) {
        Optional<ActionException> preRes = pre.flatMap(f -> f.apply());
        if (preRes.isPresent()) {
            return Either.left(preRes.get());
        }

        U u = action.apply(t1, t2);

        Optional<ActionException> postRes = post.flatMap(f -> f.apply(t1, t2, u));
        if (post.isPresent()) {
            return Either.left(postRes.get());
        } else {
            return Either.right(u);
        }
    }
}
