package net.dijkstracula.irving.protocols.chainrep;

import net.dijkstracula.melina.stdlib.collections.Vector;

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
}
