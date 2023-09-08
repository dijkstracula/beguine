package net.dijkstracula.runtime.nway;

import net.dijkstracula.melina.actions.Action0;
import net.dijkstracula.melina.runtime.Protocol;

public class MultiProtocol {
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
                    count = ((count + 1) < 0 ? 0 : (count + 1)) % 16;
                    System.out.println("[SPEC] inc: " + count);
                    return old;
                });
                addAction("mutator.inc", inc);
                dec.on(() -> {
                    long old = count;
                    count = ((count - 1) < 0 ? 0 : (count - 1)) % 16;
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

    public class ImplProxy extends Protocol {
        protected Action0<Long> inc = new Action0<>();
        protected Action0<Long> dec = new Action0<>();

        TinyCounter impl = new TinyCounter();

        public ImplProxy() {
            inc.on(() -> {
                long y = impl.getState();
                impl.increment();
                return y;
            });
            addAction("inc", inc);

            dec.on(() -> {
                long y = impl.getState();
                impl.decrement();
                return y;
            });
            addAction("dec", dec);
        }
    }
}
