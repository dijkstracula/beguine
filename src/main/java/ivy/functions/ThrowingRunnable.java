package ivy.functions;

@FunctionalInterface
public interface ThrowingRunnable<E extends Exception> {
    void run() throws E;

    static <T, E extends Exception> ThrowingRunnable<E> from(ThrowingSupplier<T, E> s) {
        return () -> s.get();
    }
}
