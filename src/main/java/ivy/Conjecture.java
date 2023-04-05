package ivy;

import ivy.exceptions.IvyExceptions;
import ivy.functions.ThrowingRunnable;

import java.util.function.Supplier;

public class Conjecture implements ThrowingRunnable<IvyExceptions.ConjectureFailure> {
    private final String desc;
    private final Supplier<Boolean> pred;

    public Conjecture(String desc, Supplier<Boolean> pred) {
        this.desc = desc;
        this.pred = pred;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public void run() throws IvyExceptions.ConjectureFailure {
        if (!pred.get()) {
            throw new IvyExceptions.ConjectureFailure(this);
        }
    }
}
