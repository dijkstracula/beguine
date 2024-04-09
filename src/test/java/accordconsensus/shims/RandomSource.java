package accordconsensus.shims;

import beguine.runtime.Arbitrary;
import scala.Int;

public class RandomSource implements accord.utils.RandomSource {

    private Arbitrary arbitrary;

    public RandomSource(Arbitrary a) {
        arbitrary = a;
    }

    @Override
    public void nextBytes(byte[] bytes) {

    }

    @Override
    public boolean nextBoolean() {
        return arbitrary.bool();
    }

    @Override
    public int nextInt() {
        return arbitrary.numeric(Int.MinValue(), Int.MaxValue());
    }

    @Override
    public long nextLong() {
        return arbitrary.asScala().nextLong();
    }

    @Override
    public float nextFloat() {
        return arbitrary.asScala().nextFloat();
    }

    @Override
    public double nextDouble() {
        return arbitrary.asScala().nextFloat();
    }

    @Override
    public double nextGaussian() {
        return 0;
    }

    @Override
    public void setSeed(long seed) {
        throw new RuntimeException("Nah.");
    }

    @Override
    public accord.utils.RandomSource fork() {
        return new RandomSource(arbitrary);
    }
}
