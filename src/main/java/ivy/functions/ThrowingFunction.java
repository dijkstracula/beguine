package ivy.functions;

import java.util.function.Function;

@FunctionalInterface
public interface ThrowingFunction<T, U, E extends Exception> {
    U apply(T t) throws E;
}
