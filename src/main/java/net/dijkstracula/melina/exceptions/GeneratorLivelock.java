package net.dijkstracula.melina.exceptions;

/**
 * Thrown internally when the generator for an Action seems to never satsify the precondition.
 */
public class GeneratorLivelock extends RuntimeException {
    public GeneratorLivelock() {
        super("Action is livelocked");
    }
}
