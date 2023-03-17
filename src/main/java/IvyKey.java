import accord.api.RoutingKey;
import accord.primitives.Range;
import accord.primitives.RoutableKey;

import java.util.Objects;

/**
 * Models the Ivy instantiation of keys (unbounded nat)
 * TODO: Originally this was backed by a BigInt.  I changed it but I don't remember why?
 */
public class IvyKey implements RoutableKey, RoutingKey {
    public final long val;

    public IvyKey(long v) {
        this.val = v;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IvyKey ivyKey = (IvyKey) o;
        return val == ivyKey.val;
    }

    @Override
    public int hashCode() {
        return Objects.hash(val);
    }

    @Override
    public String toString() {
        return "IvyKey{" +
                "val=" + val +
                '}';
    }

    @Override
    public int compareTo(RoutableKey that) {
        return Long.compare(val, ((IvyKey)that).val);
    }

    @Override
    public RoutingKey toUnseekable() {
        return this;
    }

    @Override
    public Range asRange() {
        return Range(new IvyKey(val-1), new IvyKey(val));
    }

    public static Range Range(IvyKey start, IvyKey end) {
        return new Range.EndInclusive(start, end);
    }

    public static final IvyKey MIN = new IvyKey(0);
    public static final IvyKey MAX = new IvyKey(Long.MAX_VALUE);
    public static final Range FULL_RANGE = Range(MIN, MAX);
}
