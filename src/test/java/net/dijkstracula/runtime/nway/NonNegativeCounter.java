package net.dijkstracula.runtime.nway;

import io.vavr.Tuple2;
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

    public class Proxy extends Protocol {
        private Action0<Tuple2<Void, Void>> inc = new Action0<>();
        private Action0<Tuple2<Void, Void>> dec = new Action0<>();

        // XXX: impl is a proxy?
        public Proxy(Isolate spec, Isolate impl) {
            inc.on(() -> {
                // XXX: actually, look up inc in the hashmap of actions so the
                // history in each are updated.
                Void s = spec.mutator.inc.apply();
                Void i = impl.mutator.inc.apply();
                return new Tuple2<>(s, i);
            });
        }
    }
}

