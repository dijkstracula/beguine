package ivy;

import com.microsoft.z3.*;

import io.vavr.control.Either;
import ivy.decls.Decls;
import ivy.exceptions.IvyExceptions.*;
import ivy.sorts.IvySort;
import ivy.sorts.Sorts;

import java.util.*;
import java.util.function.Supplier;

/**
 * I guess solver stuff at the im.module layer??  S
 * TODO: Is this actually the top-level class?  If so, is it better named "module" (per Ivy) or "Protocol"?
 */
public abstract class Protocol {

    protected final Random random;

    private final Context ctx;
    private final Solver slvr;

    protected final Sorts sorts;
    protected final Decls decls;

    private List<Conjecture> conjectures;

    // NB: since these are actions driven by the external environment, I guess they have to be only void-producing.
    private List<Supplier<Either<ActionException, Void>>> actions;

    public Protocol(Random r) {
        ctx = new Context();
        slvr = ctx.mkSolver();
        sorts = new Sorts(ctx, r);
        decls = new Decls(ctx, sorts, r);

        actions = new ArrayList<>();
        conjectures = new ArrayList<>();

        random = r;
    }

    public void push() { slvr.push(); }
    public void pop() { slvr.pop(); }

    protected void addConjecture(String desc, Supplier<Boolean> pred) {
        Objects.requireNonNull(desc);
        Objects.requireNonNull(pred);
        conjectures.add(new Conjecture(desc, pred));
    }
    protected void addConjecture(Conjecture conj) {
        Objects.requireNonNull(conj);
        conjectures.add(conj);
    }

    public List<Conjecture> getConjectures() { return conjectures; }

    private Either<ConjectureFailure, Void> checkConjectures() {
        for (Conjecture conj : conjectures) {
            System.out.println(String.format("Checking conjecture \"%s\"...", conj.getDesc()));
            Optional<ConjectureFailure> res = conj.get();
            if (res.isPresent()) {
                return Either.left(res.get());
            }
        }
        return Either.right(null);
    }

    public <U> void addAction(Supplier<Either<ActionException, U>> piped) {
        actions.add(() -> piped.get().map(u -> null));
    }

    public List<Supplier<Either<ActionException, Void>>> getActions() { return actions; }

    //

    public void combine(Protocol other) {
        for (Supplier<Either<ActionException, Void>> action: other.actions) {
            actions.add(action);
        }
        for (Conjecture c : other.conjectures) {
            conjectures.add(c);
        }
    }
    //

    public Either<ConjectureFailure, Void> takeAction() {
        while (true) {
            Either<ActionException, Void> res = actions.get(random.nextInt(actions.size())).get();
            if (res.isLeft()) {
                // If we get back a RetryGeneration, then the action we chose has a precondition
                // that isn't currently satisfied.
                ActionException e = res.getLeft();
                if (e.getClass().equals(RetryGeneration.class)) {
                    continue;
                }
                // Safety: The only other subclass of ActionException is ConjectureFailure.
                assert(e.getClass().equals(ConjectureFailure.class));
                ConjectureFailure fail = (ConjectureFailure)res.getLeft();
                return Either.left(fail);
            }
            return checkConjectures();
        }
    }

    public void addPredicate(Expr<BoolSort> pred) {
        slvr.add(pred);
    }

    public Model solve() {
        Status s = slvr.check();
        if (s != Status.SATISFIABLE) {
            throw new RuntimeException(String.format("Got %s back from the solver", s.toString()));
        }
        return slvr.getModel();
    }
}
