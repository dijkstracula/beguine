package ivy.functions.Actions;

import io.vavr.Function0;
import io.vavr.Function1;
import io.vavr.control.Either;
import ivy.exceptions.IvyExceptions;
import ivy.exceptions.IvyExceptions.ActionException;

import javax.annotation.Nullable;
import java.security.Provider;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An action comprises a function and optional pre/post operations.
 */
public class Action0<U> implements Function0<Either<ActionException, U>> {

    private String name;

    private Optional<Function0<Either<ActionException, Void>>> pre;
    @Nullable
    private Supplier<Either<ActionException, U>> impl;
    private Optional<Function1<U, Either<ActionException, U>>> post;

    public Action0(String n) {
        name = n;
        pre = Optional.empty();
        impl = null;
        post = Optional.empty();
    }

    public Action0(String n, Supplier<Either<ActionException, U>> f) {
        name = n;
        pre = Optional.empty();
        impl = f;
        post = Optional.empty();
    }

    public Action0(String n, Runnable f) {
        name = n;
        pre = Optional.empty();
        impl = () -> { f.run(); return Either.right(null); };
        post = Optional.empty();
    }

    public void on(Supplier<Either<ActionException, U>> f) {
        if (impl != null) {
            throw new RuntimeException(String.format("impl already set for action %s", name));
        }
        impl = f;
    }

    @Override
    public Either<ActionException, U> apply() {
        if (impl == null) {
            return Either.left(new IvyExceptions.Unimplemented(name));
        }

        Either<ActionException, Void> ret = Either.right(null);
        if (pre.isPresent()) {
            ret = pre.get().apply();
        }

        Either<ActionException, U> impl_ret = ret.flatMap((v) -> impl.get());

        return impl_ret;
    }
}
