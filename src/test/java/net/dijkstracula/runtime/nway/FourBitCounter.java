package net.dijkstracula.runtime.nway;

import net.dijkstracula.melina.actions.Action0;
import net.dijkstracula.melina.runtime.Protocol;
import net.dijkstracula.melina.runtime.Tee;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class FourBitCounter {

    public static class Spec extends Protocol {
        public Spec() {
            super(new Random(42));
        }

        class IvyObj_mutator {

            private Action0<Long> inc = new Action0<>();
            private Action0<Long> dec = new Action0<>();

            private long count;

            public IvyObj_mutator() {
                count = 0;
                inc.on(() -> {
                    System.out.println("[SPEC] inc: " + count);
                    count = (count + 1) < 0 ? 0 : (count + 1);
                    count = count % 16;
                    return count;
                });
                addAction("inc", inc);
                dec.on(() -> {
                    System.out.println("[SPEC] dec: " + count);
                    if (count == 0) {
                        count = 15;
                    } else {
                        count = (count - 1) < 0 ? 0 : (count - 1);
                    }
                    return count;
                });
                addAction("dec", dec);

                addConjecture("valid-four-byte-value", () -> count >= 0 && count < 16);
            } //cstr
        } // Ivyobj_mutator

        public IvyObj_mutator mutator = new IvyObj_mutator();
    }

    public class Counter {
        private long state;

        public Counter() {
            state = 0;
        }

        public void increment() {
            state = (state + 1) % 16;
        }

        public void decrement() {
            state = (state - 1 + 16) % 16;
        }

        public long getState() {
            return state;
        }
    }

    public class CounterProxy extends Protocol {
        private Action0<Long> inc = new Action0<>();
        private Action0<Long> dec = new Action0<>();

        private Counter state;

        public CounterProxy() {
            super(new Random(42));

            state = new Counter();

            inc.on(() -> {
                state.increment();
                return state.getState();
            });
            addAction("inc", inc);
            dec.on(() -> {
                state.decrement();
                return state.getState();
            });
            addAction("dec", dec);
        }
    }

    @Test
    public void testCounter() {
        Tee<Spec, CounterProxy> t = new Tee<>(new Random(42), new Spec(), new CounterProxy());

        // The behaviour of the two protocols under test should, of course, be
        // identical.
        for (int i = 0; i < 1000; i++) {
            t.run();
        }
    }
}
