package accordconsensus.shims;

import accord.api.MessageSink;
import accord.impl.mock.Network;
import accord.local.AgentExecutor;
import accord.local.Node;
import accord.messages.Callback;
import accord.messages.Reply;
import accord.messages.ReplyContext;
import accord.messages.Request;

public class Socket implements MessageSink {

    private final Node.Id id;
    private final OverlayNetwork net;

    private int nextMsgId;

    public Socket(Node.Id id, OverlayNetwork net) {
        this.id = id;
        this.net = net;
        this.nextMsgId = 0;
    }

    @Override
    public void send(Node.Id to, Request request) {
        net.send(this.id, to, request, null, null);
    }

    @Override
    public void send(Node.Id to, Request request, AgentExecutor executor, Callback<?> callback) {
        net.send(this.id, to, request, executor, callback);
    }

    @Override
    public void reply(Node.Id replyingToNode, ReplyContext replyContext, Reply reply) {
        net.reply(this.id, replyingToNode, Network.getMessageId(replyContext), reply);
    }

    @Override
    public void replyWithUnknownFailure(Node.Id replyingToNode, ReplyContext replyContext, Throwable failure) {
        net.reply(this.id, replyingToNode, Network.getMessageId(replyContext), new Reply.FailureReply(failure));
    }
}
