package net.dijkstracula.irving.sorts;

import com.microsoft.z3.DatatypeSort;
import com.microsoft.z3.Expr;
import net.dijkstracula.melina.actions.Action0;
import net.dijkstracula.melina.actions.Action1;
import net.dijkstracula.melina.runtime.MelinaContext;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

public class ClassTest {

    class Point {
        public long x;
        public long y;

        Action0<Long> norm = new Action0<>(() -> {
            return PointFactory.norm.apply(this);
        });
    }

    class PointFactory extends IvyClass<Point> {

        // Actions
        static Action1<Point, Long> norm = new Action1<>();

        // Fields
        static IvySort<Long, ?> x;
        static IvySort<Long, ?> y;

        public PointFactory(MelinaContext ctx) {
            super("point", ctx);
            x = new UnboundedSequence(ctx);
            y = new UnboundedSequence(ctx);
        }

        @Override
        public Point make() {
            return new Point();
        }

        @Override
        public Supplier<Point> generator() {
            Supplier<Long> x_gen = x.generator();
            Supplier<Long> y_gen = y.generator();

            return () -> {
                Point ret = make();
                ret.x = x_gen.get();
                ret.y = y_gen.get();
                return ret;
            };
        }

        @Override
        public Expr<DatatypeSort<?>> to_solver(Point val) {
            return null;
        }

        @Override
        public DatatypeSort<?> getZ3Sort() {
            return null;
        }
    }

    @Test
    public void testIvyClass() {

    }
}
