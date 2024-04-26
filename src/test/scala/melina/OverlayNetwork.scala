package melina

import accord.api.MessageSink
import accord.impl.basic.Packet
import accord.impl.mock.Network
import accord.local.Node.Id
import accord.local.{AgentExecutor, Node}
import accord.messages.{Callback, Reply, ReplyContext, Request}
import melina.OverlayNetwork.NoConnectionError

import scala.collection.mutable

// A hand-written version of Ivy's tcp_test, with in-order reliable transmission.
object OverlayNetwork {
  case class NoConnectionError(dst: Node.Id) extends Exception
}

class OverlayNetwork(nodeLookup: Function[Node.Id, Option[Node]]) {

  class Socket(self: Node.Id, net: OverlayNetwork) extends MessageSink {
    override def send(to: Node.Id, request: Request): Unit =
      net.send(self, to, request, None)

    override def send(to: Node.Id, request: Request, executor: AgentExecutor, callback: Callback[_]): Unit = {
      // XXX: we discard the executor now.  What if this isn't okay?
      net.send(self, to, request, Some(callback))
    }

    override def reply(to: Node.Id, replyContext: ReplyContext, reply: Reply): Unit =
      net.reply(self, to, Network.getMessageId(replyContext), reply)

    override def replyWithUnknownFailure(to: Node.Id, replyContext: ReplyContext, failure: Throwable): Unit =
      reply(to, replyContext, new Reply.FailureReply(failure))
  }

  val inFlight: mutable.Map[Node.Id, mutable.ArrayDeque[Packet]] = mutable.Map.empty

  val pendingCallbacks: mutable.Map[Long, Callback[Reply]] = mutable.Map.empty

  var nextMsgId = 0L

  def dial(self: Node.Id) = {
    if (inFlight.get(self).isEmpty) {
      // XXX: throw on reconnect instead?
      inFlight.put(self, mutable.ArrayDeque.empty)
    }
    new Socket(self, this)
  }

  def send(from: Node.Id, to: Node.Id, req: Request, cb: Option[Callback[_]]): Unit = {
    val msgId = getNextMessageId

    inFlight.get(to)
      .getOrElse(throw NoConnectionError(to))
      .append(new Packet(from, to, getNextMessageId, req))
    cb.foreach(cb => pendingCallbacks.put(msgId, cb.asInstanceOf[Callback[Reply]]))
  }

  def reply(from: Node.Id, to: Node.Id, replyId: Long, reply: Reply): Unit = {
    inFlight.get(to)
      .getOrElse(throw NoConnectionError(to))
      .append(new Packet(from, to, replyId, reply))
  }

  def deliver(to: Node.Id) = {
    val p = inFlight.get(to).getOrElse(throw NoConnectionError(to)).removeHead()
    val n = nodeLookup(to).getOrElse(throw NoConnectionError(to))
    val cb = pendingCallbacks.remove(p.requestId)
    p.message match {
      case req: Request => n.receive(req, p.dst, Network.replyCtxFor(p.requestId))
      case rep: Reply.FailureReply => cb.foreach(cb => cb.onFailure(p.src, rep.failure))
      case rep: Reply => cb.foreach(cb => cb.onSuccess(p.src, rep))

      case _ => throw new RuntimeException("TODO: What is a " + n)
    }
  }

  def getNextMessageId = {
    val ret = nextMsgId
    nextMsgId += 1
    ret
  }

}
