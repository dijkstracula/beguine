package net.dijkstracula.runtime.protocols;

import net.dijkstracula.melina.actions.Action0;
import net.dijkstracula.melina.exceptions.ConjectureFailure;
import net.dijkstracula.melina.runtime.MelinaContext;
import net.dijkstracula.melina.runtime.Protocol;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NonNegativeCounter {
    public static class Isolate extends Protocol {

        public Isolate(MelinaContext ctx) {
            super(ctx);
        }

        class IvyObj_mutator {
            private final Action0<Void> inc = new Action0<>();
            private final Action0<Void> dec = new Action0<>();


            private long count;

            public IvyObj_mutator() {
                count = 1;
                inc.on(() -> {
                    System.out.println("inc: " + count);
                    count = (count + 1) < 0 ? 0 : (count + 1);
                    return null;
                });
                exportAction("mutator.inc", inc);
                dec.on(() -> {
                    System.out.println("dec: " + count);
                    count = (count - 1) < 0 ? 0 : (count - 1);
                    return null;
                });
                exportAction("mutator.dec", dec);
                addConjecture("count-is-single-digit", () -> {
                    return count < 10;
                });

            } //cstr
        }
        public IvyObj_mutator mutator = new IvyObj_mutator();
    }

    @Test
    public void testCounter() {
        Isolate iso = new Isolate(MelinaContext.fromSeed(42));

        // Eventually the counter will increase to the point where the conjecture fails.
        Assertions.assertThrows(ConjectureFailure.class, () -> {
            for (int i = 0; i < 1000; i++) {
                iso.run();
            }
        });
    }
}
