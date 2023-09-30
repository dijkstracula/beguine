package net.dijkstracula.melina.utils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/** A PRNG that allows for forking and joining generator state, which is needed for n-way testing. */
public class Random implements Cloneable {

    private long state;

    public Random(long state) {
        this.state = state;
    }

    private long next(int bits) {
        return (next()) >>> (48 - bits);
    }

    private long next() {
        long ret = state;
        System.out.println(String.format("[Random] %x", ret));
        // c/o Functional Programming in Scala, Second Edition
        state = (state * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
        return ret;
    }

    public boolean nextBool() {
        long rand = next(1);
        return (rand & 1) == 0;
    }

    public int nextInt() {
        long rand = next(32);
        return (int)(rand);
    }

    public long nextLong() {
        return next();
    }

    public long nextBounded(long bound) {
        // c/o https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Random.html
        if (bound <= 0)
            throw new IllegalArgumentException("bound must be positive");

        if ((bound & -bound) == bound)  // i.e., bound is a power of 2
            return ((bound * next(31)) >> 31);

        long bits, val;
        do {
            bits = next(31);
            val = bits % bound;
        } while (bits - val + (bound-1) < 0);
        return val;
    }
    public long nextBounded(long min, long max) {
        assert(min < max);
        return nextBounded(max - min) + min;
    }

    public <T> T nextElm(List<T> v) {
        int idx = (int)nextBounded(0, v.size());
        return v.get(idx);
    }

    public <K, V> Map.Entry<K, V> nextElm(Map<K, V> m) {
        List<Map.Entry<K, V>> v = m.entrySet().stream().toList();
        return nextElm(v);
    }

    @Override
    protected Random clone() throws CloneNotSupportedException {
        return new Random(state);
    }
}
