package accordconsensus;

import beguine.runtime.Arbitrary;

import accord.local.Node.Id;
import beguine.sorts.Number;

import java.util.stream.Stream;

import static accord.impl.PrefixedIntHashKey.ranges;

public class Wrapper {
    public static void run(Arbitrary a) {
        Stream<Id> ids = new Number(0, 3).inhabitantsAsJava().map(i -> new Id(i));

    }
}
