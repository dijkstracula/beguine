package net.dijkstracula.melina.exceptions;

/**
 * Thrown internally when the argument to an action violates an internal precondition
 * and needs to be retried.  Should never be exposed externally.
 */
public class ActionArgGenRetryException extends RuntimeException {
    public ActionArgGenRetryException() {
        super("Ghost code condition necessitates retrying action generation");
    }
}
