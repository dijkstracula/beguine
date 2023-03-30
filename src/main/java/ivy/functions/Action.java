package ivy.functions;

import ivy.Conjecture.ConjectureFailure;
import org.javatuples.Pair;

import java.util.Optional;

/**
 * An action comprises a function and optional pre/post operations.
 */
public class Action<T, U> implements ThrowingFunction<T, U, ConjectureFailure> {
    private final Optional<ThrowingRunnable<ConjectureFailure>> pre;
    private final ThrowingFunction<T, U, ConjectureFailure> action;

    private final Optional<ThrowingConsumer<Pair<T, U>, ConjectureFailure>> post;

    public Action(ThrowingFunction<T, U, ConjectureFailure> f) {
        pre = Optional.empty();
        action = f;
        post = Optional.empty();
    }

    @Override
    public U apply(T t) throws ConjectureFailure {
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
