package ivy.exceptions;

import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Sort;
import ivy.Conjecture;

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

    // TODO: only the specification can throw such an exception: rename it to reflect this.
    public static abstract class ActionException extends Exception {
        public ActionException(String msg) {
            super(msg);
        }
    }

    public static class ConjectureFailure extends ActionException {
        public final Conjecture conj;
        public ConjectureFailure(Conjecture conj) {
            super(String.format("Conjecture \"%s\" failed", conj.getDesc()));
            this.conj = conj;
        }
    }

    public static class RetryGeneration extends ActionException {
        public RetryGeneration() {
            super("Ghost code condition necessitates retrying action generation");
        }
    }
}
