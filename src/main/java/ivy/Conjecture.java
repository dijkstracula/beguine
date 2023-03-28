package ivy;

import ivy.functions.ThrowingRunnable;

import java.util.function.Supplier;

public class Conjecture implements ThrowingRunnable<Conjecture.ConjectureFailure> {
    private final String desc;
    private final Supplier<Boolean> pred;

    public Conjecture(String desc, Supplier<Boolean> pred) {
        this.desc = desc;
        this.pred = pred;
    }

    @Override
    public void run() throws ConjectureFailure {
        if (!pred.get()) {
            throw new ConjectureFailure();
        }
    }

    public class ConjectureFailure extends Exception {
        public ConjectureFailure() {
            super(String.format("Conjecture \"%s\" failed", Conjecture.this.desc));
        }
    }
}
