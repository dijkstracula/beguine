package irving;

import io.vavr.control.Either;
import ivy.Protocol;
import ivy.exceptions.IvyExceptions;
import ivy.functions.Actions.*;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class CounterTest {
    public class P extends Protocol {
        public P(Random r) {
            super(r);
            addAction(mutator.dec);
            addAction(mutator.inc);
        }

        class IvyObj_mutator {
            protected Action0<Void> inc = new Action0<>("inc");

            protected Action0<Void> dec = new Action0<>("dec");

            private long count;

            public IvyObj_mutator() {
                ; // Interpret(InterpretDecl { name: "t", sort: Resolved(Number) })
                ;count = 1; // AfterAction(ActionMixinDecl { name: ["init"], params: None, ret: None, body: [ActionSequence([Assign(AssignAction { lhs: Symbol(Symbol { id: "count", sort: ToBeInferred }), rhs: Number(1) })])] })
                inc.on(() -> {
                    Void _ret = null;
                    count = count + 1;
                    return Either.right(_ret);
                }); // Implement(ImplementDecl { name: ["inc"], params: None, ret: None, body: Some([ActionSequence([Assign(AssignAction { lhs: Symbol(Symbol { id: "count", sort: ToBeInferred }), rhs: BinOp(BinOp { lhs: Symbol(Symbol { id: "count", sort: ToBeInferred }), op: Plus, rhs: Number(1) }) })])]) })
                ;dec.on(() -> {
                    Void _ret = null;
                    count = count - 1;
                    return Either.right(_ret);
                }); // Implement(ImplementDecl { name: ["dec"], params: None, ret: None, body: Some([ActionSequence([Assign(AssignAction { lhs: Symbol(Symbol { id: "count", sort: ToBeInferred }), rhs: BinOp(BinOp { lhs: Symbol(Symbol { id: "count", sort: ToBeInferred }), op: Minus, rhs: Number(1) }) })])]) })
                ;addConjecture("conj1", () -> count >= 0); // Invariant(Pred(BinOp(BinOp { lhs: Symbol(Symbol { id: "count", sort: ToBeInferred }), op: Ge, rhs: Number(0) })))
                ;
            } //cstr
        }
        IvyObj_mutator mutator = new IvyObj_mutator();
    }

    @Test
    public void testCounter() {
        Random r = new Random(42);
        P proto = new P(r);

        // At some point, the counter will go negative, invalidating the
        // nonnegativity conjecture.
        while (true) {
            Either<IvyExceptions.ConjectureFailure, Void> res = proto.takeAction();
            if (res.isLeft()) {
                break;
            }
        }
    }
}
