package net.dijkstracula.runtime.protocols;

import net.dijkstracula.melina.actions.Action0;
import net.dijkstracula.melina.exceptions.ConjectureFailureException;
import net.dijkstracula.melina.runtime.Protocol;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class NonNegativeCounter {
    public static class Isolate extends Protocol {

        public Isolate(Random r) {
            super(r);
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
                addAction("mutator.inc", inc);
                dec.on(() -> {
                    System.out.println("dec: " + count);
                    count = (count - 1) < 0 ? 0 : (count - 1);
                    return null;
                });
                addAction("mutator.dec", dec);
                addConjecture("count-is-single-digit", () -> {
                    return count < 10;
                });

            } //cstr
        }
        public IvyObj_mutator mutator = new IvyObj_mutator();
    }

    @Test
    public void testCounter() {
        Random r = new Random(42);
        Isolate iso = new Isolate(r);

        // Eventually the counter will increase to the point where the conjecture fails.
        Assertions.assertThrows(ConjectureFailureException.class, () -> {
            for (int i = 0; i < 1000; i++) {
                iso.run();
            }
        });
    }
}
