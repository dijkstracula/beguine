package net.dijkstracula.melina.exceptions;

public class IrvingCodegenException extends MelinaException {
    public IrvingCodegenException(String msg) {
        super(msg);
    }

    public static class UndefinedActionCall extends IrvingCodegenException {
        public UndefinedActionCall() {
            super("Attempt to call an undefined Action (did Irving fail to emit a .on() call?)");
        }
    }

    public static class RedefinedActionCall extends IrvingCodegenException {
        public RedefinedActionCall() {
            super("Attempt to redefined an already-defined action (did Irving emit multiple .on() calls?)");
        }
    }
}
