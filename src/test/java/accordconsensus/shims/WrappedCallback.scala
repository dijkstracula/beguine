package accordconsensus.shims

import accord.local.{AgentExecutor, Node}
import accord.messages.Reply
import accord.utils.async.AsyncChains

import java.util.concurrent.Callable

// Not unlike SafeCallback, except we ensure the AgentExecutor blocks before returning.
class WrappedCallback[T <: Reply](executor: AgentExecutor, cb: accord.messages.Callback[T]) {
  def onSuccess(id: Node.Id, reply: T): Unit = callAndBlock(id, () => cb.onSuccess(id, reply))
  def onFailure(from: Node.Id, failure: Throwable): Unit = callAndBlock(from, () => cb.onFailure(from, failure))
  def onSlowResponse(from: Node.Id) = callAndBlock(from, () => cb.onSlowResponse(from))

  private def callAndBlock[T](id: Node.Id, f: () => T) = {
    // XXX: Creating an anonymous class like this is silly.
    val c = new Callable[T] {
      override def call(): T = try {
        f()
      } catch {
        case e: Throwable => throw e
      }
    }

    AsyncChains.getBlocking(executor.submit(c))
  }
}
