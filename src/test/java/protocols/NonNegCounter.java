package protocols;

import beguine.runtime.Arbitrary;
import beguine.runtime.Protocol;


public class NonNegCounter extends Protocol {
    int mutator__count;

    public NonNegCounter(Arbitrary a) {
        super(a);

        exported("ext:mutator.inc", this::ext__mutator__inc);
        exported("ext:mutator.dec", this::ext__mutator__dec);
        exported("ext:mutator.dec-2", this::ext__mutator__dec);

        conjectured("mutator.nonnegativity", "007_counter.ivy", 34, () -> mutator__count >= 0);

        mutator__count = 1;
    }

    public void ext__mutator__inc() {
        mutator__count = mutator__count + 1;
        mutator__show(mutator__count);
        assertThat("007_counter.ivy", 31, true, "true");
    }
    public void ext__mutator__dec() {
        mutator__count = mutator__count - 1;
        mutator__show(mutator__count);
    }
    public void mutator__show(int c) {
        imp__mutator__show(c);
    }
    public void imp__mutator__show(int c) {
        debug("mutator.show " + c);
    }
}