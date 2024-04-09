package accordconsensus.shims;

import accord.impl.basic.Packet;
import accord.impl.mock.Network;
import accord.local.AgentExecutor;
import accord.local.Node;
import accord.messages.Callback;
import accord.messages.Reply;
import accord.messages.Request;

import javax.annotation.Nullable;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class OverlayNetwork {

    private final Map<Node.Id, Socket> connections;
    private final Map<Node.Id, Deque<Packet>> inFlight;

    // TODO: This should perhaps map to a thing that holds an executor as well as the callback,
    //  like SafeCallback?
    private final Map<Long, Callback<Reply>> callbacks;

    private long nextMessageId;

    public OverlayNetwork() {
        this.connections = new HashMap<>();
        this.inFlight = new HashMap<>();
        this.callbacks = new HashMap<>();
        this.nextMessageId = 0L;
    }

    public Socket createSink(Node.Id id) {
        Socket s = new Socket(id, this);
        connections.put(id, s);
        inFlight.put(id, new LinkedList<>());
        return s;
    }

    public void deliverOneSuccessfully(Node.Id to, Node.Id from, Reply reply) {
        Deque<Packet> d = this.inFlight.get(to);
        assert d.size() > 0;
        Packet delivered = d.pop();
        Callback cb = this.callbacks.remove(delivered.requestId);
        if (cb != null) {
            cb.onSuccess(from, reply);
        }
    }

    public void send(Node.Id from, Node.Id to, Request request, @Nullable AgentExecutor executor, @Nullable Callback callback) {
        long msgId = getNextMessageId();
        Packet p = new Packet(from, to, msgId, request);
        inFlight.get(to).push(p);
        if (callback != null) {
            callbacks.put(msgId, callback);
        }
    }

    public void reply(Node.Id from, Node.Id to, long replyId, Reply reply) {
        Packet p = new Packet(from, to, replyId, reply);
        inFlight.get(to).push(p);
    }

    private long getNextMessageId() {
        return nextMessageId++;
    }
}
