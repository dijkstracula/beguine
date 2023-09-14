package net.dijkstracula.irving.sorts;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntSort;

import java.util.Iterator;
import java.util.Random;
import java.util.stream.LongStream;

public class Sorts {
    private final Context ctx;
    private final Random random;

    public Sorts(Context ctx, Random random) {
        this.ctx = ctx;
        this.random = random;
    }

    public UnboundedSequence mkUnboundedSequnce() {
        return new UnboundedSequence();
    }

    public Range mkRange(String name, long min, long max) {
        return new Range(name, min, max);
    }

    public class UnboundedSequence extends IvySort<Long, IntSort> {
        public UnboundedSequence() {
            super("unbounded_sequence", () -> Math.abs(random.nextLong()));
        }

        @Override
        public Expr<IntSort> to_solver(Long val) {
            return ctx.mkInt(val);
        }

        @Override
        public IntSort getZ3Sort() {
            return ctx.getIntSort();
        }
    }

    public class Range extends IvySort<Long, IntSort> {
        long min;
        long max;

        public Range(String name, long min, long max) {
            super(name, () -> (Math.abs(random.nextLong()) % (max - min)) + min);
            assert(min < max);
            this.min = min;
            this.max = max;
        }

        @Override
        public Expr<IntSort> to_solver(Long val) {
            return ctx.mkInt(val);
        }

        @Override
        public IntSort getZ3Sort() {
            return ctx.getIntSort();
        }

        public LongStream iterator() {
            return LongStream.range(min, max);
        }
    }
}
