package net.dijkstracula.melina.stdlib.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Hand-written extraction for collections.Vector

public class Vector<T> implements Cloneable {
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

    public int end() {
        return this.backing.size();
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

    @Override
    protected Vector<T> clone() {
        Vector<T> ret = new Vector<>();
        ret.backing = new ArrayList<>(backing);
        return ret;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("[");
        for (int i = 0; i < end(); i++) {
            if (i > 0) {
                ret.append(",");
            }
            ret.append(get(i));
        }
        ret.append("]");

        return ret.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector<?> vector = (Vector<?>) o;
        return Objects.equals(backing, vector.backing);
    }

    @Override
    public int hashCode() {
        return Objects.hash(backing);
    }
}
