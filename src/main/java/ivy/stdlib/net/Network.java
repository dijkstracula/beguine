package ivy.stdlib.net;

/**
 * Mocks an asynchronous network.
 * @param <Id> The routing key for the network (e.g. a network node ID)
 * @param <M> The message class to be routed over the network.
 */
public interface Network<Id, M> {

    /**
     * Enqueues a message to be transferred over the network mock.
     * @param self The ID of the sender.
     * @param dst The ID of to whom the message should be routed.
     * @param msg The message to be routed.
     */
    Void send(Id self, Id dst, M msg);

    /**
     * Invoked when a message arrives at a particular node.
     * @param self the recipient of the message.
     * @param src the sender of the message.
     * @param msg The message that has been routed.
     */
    //public Function3<Id, Id, M, Void> recv = (self, src, msg) -> { throw new RuntimeException("Not implemented"); };
    Void recv(Id self, Id src, M msg);
}
