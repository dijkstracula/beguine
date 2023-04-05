package ivy.functions;

import ivy.exceptions.IvyExceptions;
import org.javatuples.Pair;

import java.util.Optional;
import java.util.function.Function;

/**
 * An action comprises a function and optional pre/post operations.
 */
public class Action<T, U> implements ThrowingFunction<T, U, IvyExceptions.ConjectureFailure> {
    private final Optional<ThrowingRunnable<IvyExceptions.ConjectureFailure>> pre;
    private final Function<T, U> action;

    private final Optional<ThrowingConsumer<Pair<T, U>, IvyExceptions.ConjectureFailure>> post;

    public Action(Function<T, U> f) {
        pre = Optional.empty();
        action = f;
        post = Optional.empty();
    }

    public Action(ThrowingRunnable<IvyExceptions.ConjectureFailure> pre,
                  Function<T, U> f,
                  ThrowingConsumer<Pair<T, U>, IvyExceptions.ConjectureFailure> post) {
        this.pre = Optional.of(pre);
        this.action = f;
        this.post = Optional.of(post);
    }

    @Override
    public U apply(T t) throws IvyExceptions.ConjectureFailure {
        if (pre.isPresent()) {
            pre.get().run();
        }
        U ret = action.apply(t);
        if (post.isPresent()) {
            Pair<T, U> args = Pair.with(t, ret);
            post.get().accept(args);
        }
        return ret;
    }
}
