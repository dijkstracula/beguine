package ivy;

import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Sort;

public class IvyExceptions {
    public static class DuplicateSortDefException extends RuntimeException {
        public DuplicateSortDefException(Sort s) {
            super(String.format("Sort %s already defined", s.getName()));
        }
    }

    public static class DuplicateSortRangeException extends RuntimeException {
        public DuplicateSortRangeException(Sort s) {
            super(String.format("Sort %s range already defined", s.getName()));
        }
    }

    public static class DuplicateDeclException extends RuntimeException {
        public DuplicateDeclException(FuncDecl<?> d) {
            super(String.format("Declaration %s already defined", d.getName()));
        }
    }

    public static class FuncArityMismatch extends RuntimeException {
        public FuncArityMismatch(int funcArity, int appArity) {
            super(String.format("FuncDecl of arity %d applied with %d args",
                    funcArity, appArity));
        }
    }
}
