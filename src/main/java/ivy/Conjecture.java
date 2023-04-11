package ivy;

import ivy.exceptions.IvyExceptions.*;

import java.util.Optional;
import java.util.function.Supplier;

// TODO: If this supplied an Either, then the right-bias means we could just map over a list of them.
public class Conjecture implements Supplier<Optional<ConjectureFailure>> {
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
    public Optional<ConjectureFailure> get() {
        if (this.pred.get()) {
            return Optional.empty();
        }
        return Optional.of(new ConjectureFailure(this));
    }
}
