package net.dijkstracula.melina.actions;

import net.dijkstracula.melina.exceptions.IrvingCodegenException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class Action1Tests {
    @Test
    public void testInvalidActionDefinition() {
        // No call to .on()
        Action1<Integer, Void> a = new Action1<>();
        Assertions.assertThrows(IrvingCodegenException.class, () -> {
           a.apply(42);
        });

        // Two calls to .on(); only the first should succeed.
        a.on((i) -> null);
        Assertions.assertThrows(IrvingCodegenException.class, () -> {
            a.on((i) -> null);
        });
    }

    @Test
    public void testCall() {
        Action1<Integer, Integer> a1 = new Action1<>((i) -> i+1);
        Action1<Integer, Integer> a2 = new Action1<>();
        a2.on((i) -> i+1);
        Assertions.assertEquals(a1.apply(41), a2.apply(41));
    }
}
