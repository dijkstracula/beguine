package ivy;

import ivy.functions.ThrowingConsumer;

import java.util.function.Predicate;

public class Conjecture<T> implements ThrowingConsumer<T, Conjecture.ConjectureFailure> {
    private final String desc;
    private final Predicate<T> pred;

    public Conjecture(String desc, Predicate<T> pred) {
        this.desc = desc;
        this.pred = pred;
    }

    public void accept(T t) throws ConjectureFailure {
        if (!pred.test(t)) {
            throw new ConjectureFailure(this);
        }
    }

    public static class ConjectureFailure extends Exception {
        public ConjectureFailure(Conjecture<?> conj) {
            super(String.format("Conjecture \"%s\" failed", conj.desc));
        }
    }
}
