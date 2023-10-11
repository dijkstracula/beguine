package net.dijkstracula.irving.protocols.chainrep;

import net.dijkstracula.melina.stdlib.collections.Vector;

import java.util.Objects;

public class Msg {
    // Fields
    public long kind;
    public long src;
    public long to_append;
    public Vector<Long> read_state;

    // Actions


    @Override
    public String toString() {
        return "msg_t{" +
                "kind=" + kind +
                ", src=" + src +
                ", to_append=" + to_append +
                ", read_state=" + read_state +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Msg msg = (Msg) o;
        return kind == msg.kind && src == msg.src && to_append == msg.to_append && Objects.equals(read_state, msg.read_state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, src, to_append, read_state);
    }
}
