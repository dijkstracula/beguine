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

public class OverlayNetwork implements Network {

    private final Map<Node.Id, Socket> connections;
    private final Map<Node.Id, Deque<Packet>> inFlight;

    // TODO: This should perhaps map to a thing that holds an executor as well as the callback,
    //  like SafeCallback?
    private final Map<Long, Callback<Reply>> callbacks;

    public OverlayNetwork() {
        this.connections = new HashMap<>();
        this.inFlight = new HashMap<>();
        this.callbacks = new HashMap<>();
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

    @Override
    public void send(Node.Id from, Node.Id to, Request request, @Nullable AgentExecutor agentExecutor, Nullable Callback callback) {
        // XXX: at the moment we discard the executor.  It is possible that our model-driven executor may have
        // a different type, in which case this shouldn't extend mock.Network.
    }

    @Override
    public void reply(Node.Id from, Node.Id to, long replyId, Reply reply) {

    }
}
