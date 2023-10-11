package net.dijkstracula.melina.history;

import java.util.List;

// Down the road this should look a lot like a z3.FuncDecl.
// TODO: Ivy doesn't log return values from actions.  Should we?
// TODO: how to integrate this into a span-like representation?
public record ActionCall(String funcName, List<Object> args) {
    public static ActionCall fromAction0(String funcName) {
        return new ActionCall(funcName, List.of());
    }

    public static <T1> ActionCall fromAction1(String funcName, T1 t1) {
        return new ActionCall(funcName, List.of(t1));
    }

    public static <T1, T2> ActionCall fromAction2(String funcName, T1 t1, T2 t2) {
        return new ActionCall(funcName, List.of(t1, t2));
    }

    public static <T1, T2, T3> ActionCall fromAction3(String funcName, T1 t1, T2 t2, T3 t3) {
        return new ActionCall(funcName, List.of(t1, t2, t3));
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(funcName + "(");
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) {
                s.append(",");
            }
            s.append(args.get(i));
        }
        s.append(")");

        return s.toString();
    }
}
