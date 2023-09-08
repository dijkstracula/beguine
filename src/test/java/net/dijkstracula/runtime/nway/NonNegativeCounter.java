package net.dijkstracula.runtime.nway;

import net.dijkstracula.melina.actions.Action0;
import net.dijkstracula.melina.runtime.Protocol;

public class NonNegativeCounter {

    public static class Isolate extends Protocol {
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
                addConjecture(() -> {
                    return count < 10;
                });

            } //cstr
        }
        public IvyObj_mutator mutator = new IvyObj_mutator();
    }
}

