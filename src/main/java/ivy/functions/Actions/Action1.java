package ivy.functions.Actions;

import io.vavr.Function0;
import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.control.Either;
import ivy.exceptions.IvyExceptions.ActionException;
import ivy.functions.ThrowingRunnable;

import java.util.Optional;

/**
 * An action comprises a function and optional pre/post operations.
 */
public class Action1<T, U> implements Function1<T, Either<ActionException, U>> {
    private final Optional<Function0<Optional<ActionException>>> pre;
    private final Function1<T, U> action;
    private final Optional<Function2<T, U, Optional<ActionException>>> post;

    public Action1(Optional<Function0<Optional<ActionException>>> pre,
                   Function1<T, U> f,
                   Optional<Function2<T, U, Optional<ActionException>>> post) {
        this.pre = pre;
        this.action = f;
        this.post = post;
    }

    @Override
    public Either<ActionException, U> apply(T t) {
        Optional<ActionException> preRes = pre.flatMap(f -> f.apply());
        if (preRes.isPresent()) {
            return Either.left(preRes.get());
        }

        U u = action.apply(t);

        Optional<ActionException> postRes = post.flatMap(f -> f.apply(t, u));
        if (post.isPresent()) {
            return Either.left(postRes.get());
        } else {
            return Either.right(u);
        }
    }
}
