package net.dijkstracula.runtime.nway;

import net.dijkstracula.melina.actions.Action0;
import net.dijkstracula.melina.runtime.Protocol;
import net.dijkstracula.melina.runtime.Tee;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class IdenticalProtocols {

    public static class Isolate extends Protocol {

        public Isolate(Random r) {
            super(r);
        }

        class IvyObj_mutator {
            private Action0<Void> inc = new Action0<>();
            private Action0<Void> dec = new Action0<>();


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

            } //cstr
        }
        public IvyObj_mutator mutator = new IvyObj_mutator();
    }

    @Test
    public void testCounter() {
        Random r = new Random(42);
        Tee t = new Tee(r, new Isolate(r), new Isolate(r));

        // The behaviour of the two protocols under test should, of course, be
        // identical.
        for (int i = 0; i < 1000; i++) {
            t.run();
        }
    }
}

