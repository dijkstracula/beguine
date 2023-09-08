package net.dijkstracula.runtime.nway;

import net.dijkstracula.melina.actions.Action0;
import net.dijkstracula.melina.exceptions.ConjectureFailureException;
import net.dijkstracula.melina.runtime.Driver;
import net.dijkstracula.melina.runtime.Protocol;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Composed {
    /** The reference implementation extracted from Ivy. */
    public static class ReferenceImplementation extends Protocol {
        class IvyObj_mutator {
            public Action0<Long> inc = new Action0<>();
            public Action0<Long> dec = new Action0<>();

            private long count;

            public IvyObj_mutator() {
                count = 1;
                inc.on(() -> {
                    long old = count;
                    count = (count + 1) < 0 ? 0 : (count + 1);
                    System.out.println("[SPEC] inc: " + count);
                    return old;
                });
                addAction("mutator.inc", inc);
                dec.on(() -> {
                    long old = count;
                    count = (count - 1) < 0 ? 0 : (count - 1);
                    System.out.println("[SPEC] dec: " + count);
                    return old;
                });
                addAction("mutator.dec", dec);
                addConjecture(() -> {
                    return count >= 0 && count < 20;
                });

            } //cstr
        }
        public IvyObj_mutator mutator = new IvyObj_mutator();
    }

    /** Our "actual" implementation that we care about, written by humans. */
    public class TinyCounter {
        int state;
        public TinyCounter() {
            state = 1;
        }

        public void increment() {
            state++;
            System.out.println("[IMPL] inc: " + state);
        }

        public void decrement() {
            state--;
            System.out.println("[IMPL] dec: " + state);
        }

        public int getState() {
            return state;
        }
    }

    public class RefinementProxy extends Protocol {
        protected Action0<Void> inc = new Action0<>();
        protected Action0<Void> dec = new Action0<>();

        ReferenceImplementation spec = new ReferenceImplementation();
        TinyCounter impl = new TinyCounter();
        List<String> impl_history = new ArrayList<>();

        public RefinementProxy() {
            inc.on(() -> {
                long x = spec.mutator.inc.apply();

                long y = impl.getState();
                impl.increment();
                impl_history.add(String.format("inc(%d)", y));

                return null;
            });
            addAction("inc", inc);

            dec.on(() -> {
                long x = spec.mutator.dec.apply();

                long y = impl.getState();
                impl.decrement();
                impl_history.add(String.format("dec(%d)", y));
                return null;
            });
            addAction("dec", dec);

            addConjecture(() -> {
                return impl.getState() >= 0 && impl.getState() < 20;
            });

            addConjecture(() -> {
                String slast = spec.getHistory().get(spec.getHistory().size() - 1);
                String ilast = impl_history.get(impl_history.size() - 1);
                return slast.equals(ilast);
            });
        }
    }

    @Test
    public void testCounter() {
        Driver d = new Driver(new Random(41), new RefinementProxy());

        // Eventually the counter will increase to the point where the conjecture fails.
        Assertions.assertThrows(ConjectureFailureException.class, () -> {
            for (int i = 0; i < 1000; i++) {
                d.run();
            }
        });
    }
}
