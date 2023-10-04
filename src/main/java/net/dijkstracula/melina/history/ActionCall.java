package net.dijkstracula.melina.history;

import java.util.List;

// Down the road this should look a lot like a z3.FuncDecl.
public record ActionCall(String funcName, List<Object> args, Object ret) {
    public static <U> ActionCall fromAction0(String funcName, U ret) {
        return new ActionCall(funcName, List.of(), ret);
    }

    public static <T1, U> ActionCall fromAction1(String funcName, T1 t1, U ret) {
        return new ActionCall(funcName, List.of(t1), ret);
    }

    public static <T1, T2, U> ActionCall fromAction2(String funcName, T1 t1, T2 t2, U ret) {
        return new ActionCall(funcName, List.of(t1, t2), ret);
    }

    public static <T1, T2, T3, U> ActionCall fromAction3(String funcName, T1 t1, T2 t2, T3 t3, U ret) {
        return new ActionCall(funcName, List.of(t1, t2, t3), ret);
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append(funcName + "(");
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) {
                ret.append(",");
            }
            ret.append(args.get(i));
        }
        ret.append(")");
        ret.append(" -> ");
        ret.append(ret);

        return ret.toString();
    }
}
