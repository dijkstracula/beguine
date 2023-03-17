package z3;

import org.junit.jupiter.api.Test;

import com.microsoft.z3.*;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class z3Tests {
    Context ctx;

    public z3Tests() {
        HashMap<String, String> cfg = new HashMap<>();
        cfg.put("model", "true");
        ctx = new Context(cfg);
    }
    @Test
    public void testDyLoad() {
        Solver s = ctx.mkSolver();
    }

    @Test
    public void testTrivialSolve() {
        Solver s = ctx.mkSolver();

        try {
            BoolSort Boolean = ctx.getBoolSort();
            Expr<BoolSort> a = ctx.mkConst("a", Boolean);
            Expr<BoolSort> b = ctx.mkConst("b", Boolean);

            s.add(ctx.mkEq(a, ctx.mkTrue()));
            s.add(ctx.mkImplies(a, b));
            Status status = s.check();
            assertTrue(status.equals(Status.SATISFIABLE));

            Model m = s.getModel();
            assertEquals(ctx.mkTrue(), m.eval(a, false));
            assertEquals(ctx.mkTrue(), m.eval(b, false));
        } catch (Z3Exception e) {
            throw e;
        }

    }
}
