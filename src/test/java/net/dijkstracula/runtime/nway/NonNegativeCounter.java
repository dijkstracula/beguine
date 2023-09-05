package net.dijkstracula.runtime.nway;

import net.dijkstracula.irving.sorts.Sorts;
import net.dijkstracula.melina.actions.Action0;
import net.dijkstracula.melina.runtime.Protocol;

public class NonNegativeCounter {
    /** The reference implementation extracted from Ivy. */
    public static class ReferenceImplementation extends Protocol {
        class IvyObj_mutator {
            protected Action0<Long> inc = new Action0<>();
            protected Action0<Long> dec = new Action0<>();


            private long count;

            public IvyObj_mutator() {
                count = 1;
                inc.on(() -> {
                    System.out.println("[SPEC] inc: " + count);
                    long old = count;
                    count = (count + 1) < 0 ? 0 : (count + 1);
                    return old;
                });
                addAction("mutator.inc", inc);
                dec.on(() -> {
                    System.out.println("[SPEC] dec: " + count);
                    long old = count;
                    count = (count - 1) < 0 ? 0 : (count - 1);
                    return old;
                });
                addAction("mutator.dec", dec);
                addConjecture(() -> {
                    return count < 10;
                });

            } //cstr
        }
        public IvyObj_mutator mutator = new IvyObj_mutator();
    }

    /** Our "actual" implementation that we care about, written by humans. */
    public class TinyCounter {
        int state;
        public TinyCounter() {
            state = 0;
        }

        public void increment() {
            System.out.println("[IMPL] inc: " + state);
            state++;
        }

        public void decrement() {
            System.out.println("[IMPL] dec: " + state);
            state--;
        }
    }
}
