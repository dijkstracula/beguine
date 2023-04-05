package ivy.net;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Mocks an asynchronous network.
 * @param <Id> The routing key for the network (e.g. a network node ID)
 * @param <M> The message class to be routed over the network.
 */
public interface Network<Id extends Comparable<Id>, M> {
    /**
     * Enqueues a message to be transferred over the network mock.
     * @param self The ID of the sender.
     * @param dst The ID of to whom the message should be routed.
     * @param msg The message to be routed.
     */
    void send(Id self, Id dst, M msg);

    /**
     * Associates an ID with action to take when a message is delivered to that ID.
     * @param self The ID of the callback.
     * @param callback The operation to invoke on a message recv.
     */
    //void registerRecv(Id self, BiConsumer<Id, M> callback);
}
