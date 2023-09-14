package net.dijkstracula.melina.stdlib.collections;

import java.util.ArrayList;
import java.util.List;

// Hand-written extraction for collections.Vector

public class Vector<T> {
    private List<T> backing;

    public Vector() {
        this.backing = new ArrayList<>();
    }

    public static <T> Vector<T> empty() {
        return new Vector();
    }

    public static <T> Vector<T> create(int s, T y) {
        Vector<T> ret = new Vector<>();
        for (int i = 0; i < s; i++) {
            ret.backing.add(y);
        }
        return ret;
    }

    //

    public Vector<T> append(T v) {
        Vector<T> ret = this.clone();
        ret.backing.add(v);
        return ret;
    }

    public T get(int idx) {
        return this.backing.get(idx);
    }

    public Vector<T> set(int x, T y) {
        Vector<T> ret = this.clone();
        ret.backing.set(x, y);
        return ret;
    }

    public Vector<T> extend(Vector<T> b) {
        Vector<T> ret = this.clone();
        ret.backing.addAll(b.backing);
        return ret;
    }

    protected Vector<T> clone() {
        Vector<T> ret = new Vector<>();
        ret.backing = List.copyOf(this.backing);
        return ret;
    }
}
